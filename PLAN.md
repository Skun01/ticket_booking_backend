# Kế hoạch triển khai - Hệ thống bán vé sự kiện

> Nền tảng: base project Spring Boot 4.1 (Java 17, Maven, MySQL, Liquibase, Spring Security OAuth2 Resource Server JWT HS256).
> Quyết định chốt: **giữ MySQL** (không đổi SQL Server), **giữ prefix `/api`**, **phân trang dùng JPA Specification** (không dùng JPQL).

## Khác biệt so với plan gốc (đã điều chỉnh)

| Hạng mục | Plan gốc | Thực hiện ở project này |
|---|---|---|
| Database | SQL Server | **MySQL** (giữ nguyên hạ tầng base) |
| Stored Procedure | `sp_revenue_report(@from,@to)` T-SQL | MySQL procedure `sp_revenue_report(IN p_from, IN p_to)` |
| Phân trang sự kiện | JPQL | **JPA Specification** + `Pageable` |
| Prefix endpoint | `/auth`, `/orders`... | `/api/auth`, `/api/orders`... |
| Role | USER/ADMIN | **ADMIN / ORGANIZER / CUSTOMER** |

Những phần còn lại (RabbitMQ, Jasper, native query, EntityManager, transaction, N+1, conditional update trừ kho) giữ đúng ý đồ plan gốc.

## Trạng thái hiện tại của base

- Đã có: `User`, `RefreshToken`, auth JWT + refresh cookie rotate, `GlobalExceptionHandler` (ProblemDetail + `code`), CORS, Swagger, Liquibase (changeset 001-003), `AdminAccountInitializer`.
- Role hiện tại chỉ `USER/ADMIN`; `User` chưa có `phone`, `status`; cột mật khẩu tên `password`.
- Kiến trúc: package theo layer, `service/` interface + `service/impl/`, DTO `record`, lỗi qua `AppException(<Feature>ErrorCode.X)`.

---

## Phase 0 — Nền tảng (`p0-foundation`)

**Mục tiêu:** chuẩn bị hạ tầng trước khi xây feature.

1. `pom.xml` thêm dependency:
   - `spring-boot-starter-amqp` (RabbitMQ)
   - `spring-boot-starter-mail` (gửi email)
   - `jasperreports` (JasperReports, bản tương thích Java 17)
   - `com.google.zxing:core` + `javase` (sinh QR)
2. Đổi `domain/enums/Role.java`: `ADMIN, ORGANIZER, CUSTOMER`.
3. `application.yaml`: thêm block `spring.rabbitmq.*`, `spring.mail.*`; thêm `app.order.hold-minutes: 15`, `app.rabbitmq.*` (exchange/queue/routing key), `app.mail.from`.
4. Cập nhật `SecurityConfig` role mapping cho 3 role; `AdminAccountInitializer` tạo admin role `ADMIN`.
5. Tạo `config/RabbitConfig` (khai báo `TopicExchange`, 2 `Queue`, binding, `Jackson2JsonMessageConverter`, `RabbitTemplate`).
6. Properties record trong `config/property/`: `RabbitProperties`, `MailProperties`, `OrderProperties`.

**Done khi:** project compile, app khởi động (có MySQL + RabbitMQ), role mới áp dụng.

---

## Phase 1 — Auth + phân quyền 3 role (`p1-auth`)

1. `User`: thêm `phone` (nullable), `status` (enum `UserStatus`: ACTIVE/LOCKED), method nghiệp vụ `lock()/activate()`. Liquibase changeset `004-alter-users-add-phone-status.yaml` (và cột role đã có value mới).
2. `RegisterRequest`/`AuthServiceImpl`: user đăng ký mặc định role `CUSTOMER`.
3. Endpoint giữ chuẩn base: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` (đã có `/api/users/me` — bổ sung alias `/api/auth/me` trả `UserResponse`).
4. Annotation phân quyền: thêm `@IsOrganizer`, `@IsCustomer` (giống `@IsAdmin`) hoặc dùng `@PreAuthorize("hasRole('...')")` trực tiếp.
5. `SecurityConfig`: `/api/auth/**` public; còn lại yêu cầu JWT; phân quyền chi tiết bằng `@PreAuthorize` ở controller.

**Done khi:** đăng ký ra CUSTOMER, login lấy token, 3 role truy cập đúng vùng.

---

## Phase 2 — CRUD category / venue / event / ticket-type (`p2-catalog`)

Entity (không setter, đổi trạng thái qua method; `@ManyToOne` LAZY):

- `Category(name, slug)` — changeset 005
- `Venue(name, address, city, capacity)` — changeset 006
- `Event(organizer→User, category, venue, title, description, bannerUrl, startTime, endTime, status)` với `EventStatus` DRAFT/PUBLISHED/CANCELLED/ENDED; method `publish()/cancel()/end()` — changeset 007 (index `events(status, start_time)`)
- `TicketType(event, name, price, totalQuantity, soldQuantity, maxPerOrder, saleStart, saleEnd)` — changeset 008.
  Thao tác giữ/trả kho triển khai ở Phase 4 bằng conditional update, không tăng `soldQuantity` qua API cấu hình.

Endpoint:
- Admin: `POST/PUT/DELETE /api/admin/categories`, `/api/admin/venues` (`@PreAuthorize hasRole('ADMIN')`)
- Organizer: `POST /api/organizer/events`, `PUT /api/organizer/events/{id}`, `PATCH /api/organizer/events/{id}/status`; `POST /api/organizer/events/{id}/ticket-types`, `PUT /api/ticket-types/{id}` (chỉ owner mới sửa event của mình)

DTO request/response `record`; mapping `Response.from(entity)`. ErrorCode riêng: `EventErrorCode`, `TicketTypeErrorCode`, `CatalogErrorCode`.

**Done khi:** organizer tạo được event + ticket type; admin quản lý category/venue.

Quy tắc triển khai Phase 2:
- Admin có CRUD category/venue, bao gồm danh sách phân trang; organizer có danh sách/chi tiết sự kiện của mình.
- Cấu hình event/ticket type chỉ thay đổi khi `DRAFT`; tổng vé các hạng không vượt capacity của venue.
- Publish yêu cầu sự kiện chưa bắt đầu và có hạng vé; `ENDED` chỉ khi đã qua `endTime`.
- Owner publish trực tiếp theo endpoint hiện tại; quy trình admin duyệt sự kiện riêng chưa triển khai.
- Không xóa category/venue đang được dùng, không đổi capacity của venue đã gắn sự kiện.
- DTO được map trong transaction của service; hướng dẫn API và JSON mẫu có trong `README.md`.

---

## Phase 3 — Public API xem sự kiện (`p3-public-events`) — **Specification + phân trang**

1. `EventRepository extends JpaRepository<Event,Long>, JpaSpecificationExecutor<Event>`.
2. `repository/specification/EventSpecifications`: static method build `Specification<Event>`:
   - `statusIs(PUBLISHED)` (public chỉ thấy PUBLISHED)
   - `categoryId(Long)`, `city(String)`, `startFrom(Instant)`, `startTo(Instant)`
   - kết hợp bằng `Specification.allOf(...)` theo filter có mặt.
3. `GET /api/events?categoryId=&city=&from=&to=&page=&size=` → `findAll(spec, pageable)`, map sang `PageResponse<EventSummaryResponse>` theo format phân trang Phase 2.
4. `GET /api/events/{id}`: trả chi tiết + list ticket type kèm **số vé còn lại** (`totalQuantity - soldQuantity`).
5. **N+1**: `GET /api/events/{id}` và listing fetch `category/venue` bằng `@EntityGraph`;
   collection `ticketTypes` LAZY có `@BatchSize(size = 50)`, tải riêng khi map chi tiết.
   Không fetch collection trong query phân trang. README có hướng dẫn so sánh N+1 trên branch demo.
6. Chỉ mở public `GET /api/events` và `GET /api/events/{id}`; cả hai chỉ trả `PUBLISHED`.
   Sự kiện không tồn tại hoặc không public trả `404`. Không mở public các HTTP method khác.
7. `city` so khớp đầy đủ không phân biệt hoa/thường; khoảng `startTime` là `[from, to)`.
   `page >= 0`, `size` từ 1 đến 100, thứ tự `startTime ASC, id ASC`.

**Done khi:** filter + phân trang dùng Specification, DTO được map trong transaction;
fetch strategy ngăn N+1 cho category/venue và không fetch collection khi phân trang.
Số query thực tế cần xác minh trên MySQL khi chạy demo; không xem compile là xác nhận hiệu năng runtime.

---

## Phase 4 — Luồng đặt vé + thanh toán + hết hạn (`p4-order-flow`) — phần khó nhất

Entity + changeset 009-012 (+ index `orders(user_id,created_at)`, `orders(status,expires_at)`, `tickets(ticket_code)`):
- `Order(orderCode, user, promoCode?, totalAmount, discountAmount, status, expiresAt)` — `OrderStatus` PENDING/PAID/CANCELLED/EXPIRED; method `markPaid()/cancel()/expire()`
- `OrderItem(order, ticketType, quantity, unitPrice)`
- `Ticket(orderItem, ticketCode, status, checkedInAt)` — `TicketStatus` VALID/USED/CANCELLED; method `use()`
- `Payment(order, method, amount, status, transactionRef, paidAt)` — `PaymentStatus` SUCCESS/FAILED

Luồng:
1. `POST /api/orders` `{ticketTypeId, quantity}` (CUSTOMER), `@Transactional` (REQUIRED):
   - Trừ kho bằng **conditional update** trong repository:
     `UPDATE ticket_types SET sold_quantity = sold_quantity + :qty WHERE id = :id AND sold_quantity + :qty <= total_quantity` → `@Modifying @Query(nativeQuery)`.
   - Rows affected = 0 → `throw AppException(OrderErrorCode.SOLD_OUT)` → **409**.
   - Tạo `Order` PENDING, `expiresAt = min(now + app.order.hold-minutes, event.startTime)`,
     `OrderItem` lưu `unitPrice` tại thời điểm mua. Trả **201**.
2. `POST /api/orders/{id}/pay`: giả lập cổng thanh toán → `Order.markPaid()`, sinh `Ticket` theo tổng quantity, tạo `Payment(SUCCESS)`, **publish message `order.paid`** vào RabbitMQ.
   - Payment lỗi: ghi `Payment(FAILED)` bằng `REQUIRES_NEW` để không mất bản ghi khi rollback đơn.
3. `POST /api/orders/{id}/cancel`: `Order.cancel()`, trả kho `releaseSold`, publish `order.cancelled`.
4. `GET /api/orders/my`, `GET /api/orders/{id}` (owner), `GET /api/tickets/my`.
5. `POST /api/organizer/checkin` `{ticketCode}`: VALID→USED (`Ticket.use()`), set `checkedInAt`.
6. Job `@Scheduled(fixedRate = 60s)` `OrderExpiryJob`: query `orders(status=PENDING, expiresAt<=now)`
   lấy tối đa 100 ứng viên, gọi bean `OrderExpiryService` để mỗi đơn có transaction riêng → `expire()` + trả kho.

Điểm giảng dạy cần demo (comment giải thích "tại sao"):
- **Dirty checking**: đổi `order.status` trong transaction không gọi `save()`.
- **Self-invocation**: README hướng dẫn tái hiện trên branch demo rồi khôi phục bean khác;
  code chạy chính không cố ý chứa lỗi transaction.
- **REQUIRES_NEW** cho payment FAILED / activity log.

Quy tắc triển khai Phase 4:
- Mỗi đơn mua một hạng vé, từ 1 đến 100 vé và không vượt `maxPerOrder`.
  Chỉ mua trước `startTime`, trong `[saleStart, saleEnd)` nếu có giới hạn và event đang `PUBLISHED`.
- `soldQuantity` gồm vé giữ chỗ và đã thanh toán; pay không trừ kho thêm.
  Pay/cancel/expire khóa hàng order và kiểm tra trạng thái lại; check-in khóa hàng ticket.
- Pay `PAID` và cancel `CANCELLED`/`EXPIRED` là idempotent. Chưa có idempotency key cho create.
- MOCK là phương thức duy nhất; pay không body mặc định thành công, hoặc gửi `simulateFailure=true` để demo lỗi.
  Failure rollback trước, facade không giữ transaction rồi gọi `PaymentAuditService` (`REQUIRES_NEW`)
  để tránh kiểm tra FK chờ khóa order của transaction cha.
- Đơn đến hạn được expire/trả kho ngay khi gọi pay/cancel; pay trả `409 ORDER_EXPIRED` bằng ngoại lệ riêng
  `noRollbackFor` để không rollback phần expire. Các lỗi khác vẫn rollback bình thường.
- Booking/pay/check-in giữ khóa chia sẻ event để chặn race với đổi trạng thái event.
  Cấu hình và booking cùng thứ tự khóa event → ticket type; đọc cấu hình sau khi chờ khóa dùng `READ_COMMITTED`.
- `OrderEventPublisher` gửi RabbitMQ bằng listener AFTER_COMMIT, best effort;
  chưa có outbox/confirm/retry bền vững. Consumer mail/log để Phase 5.
- Check-in chỉ owner event, ticket VALID, đơn PAID, event PUBLISHED và chưa hết `endTime`.
  Cho phép check-in trước `startTime`; chưa triển khai refund khi cancel event hay cancel đơn PAID.
- Cột `promoCode` dự phòng, discount bằng 0; chưa triển khai chương trình khuyến mãi.

**Done khi:** triển khai đủ luồng và compile không test thành công.
Checklist runtime race mua vé cuối, pay/cancel/expire đồng thời, quét vé đồng thời,
rollback payment FAILED và đối chiếu migration phải chạy trên MySQL khi demo;
không coi compile là bằng chứng các tình huống runtime đã được xác minh.

---

## Phase 5 — RabbitMQ: mail + activity log (`p5-rabbitmq`)

1. File changeset `013-create-notification-tables.yaml` thêm hai bảng:
   - `activity_logs`: `messageId, userId, action, entityType, entityId, detail, createdAt/updatedAt`;
     unique `(messageId, action)` và index `(userId, createdAt)`.
   - `mail_deliveries`: receipt gửi mail, orderId unique, messageId, sentAt, createdAt/updatedAt;
     serialize xử lý mail cho cùng order để tránh gửi lại sau khi đã ghi nhận thành công.
2. Topic exchange; routing key `order.paid`, `order.cancelled`; queue `mail.queue` (binding `order.paid`) + `log.queue` (binding `order.#`).
3. `notification/`:
   - Kế thừa `OrderEventPublisher` + `OrderMessage` đã có ở Phase 4, gửi sau commit khi pay/cancel.
   - `MailListener` `@RabbitListener(queues=mail.queue)`: gửi email vé (đính kèm PDF ở Phase 7), ACK thủ công/manual.
   - `ActivityLogListener` `@RabbitListener(queues=log.queue)`: ghi `activity_logs` (dùng `REQUIRES_NEW`).
4. Message DTO (record) serialize JSON (`Jackson2JsonMessageConverter`), trust package DTO order;
   validate field, routing key/status, AMQP messageId nếu có và đối chiếu order trong DB.
5. ACK chỉ sau transaction commit; `OrderMessageHandler` retry nghiệp vụ mặc định 2 lần (tổng 3 attempts),
   delay 1s/2s; không retry ACK. Payload sai hoặc hết retry thì reject không requeue.
   DLX `order.dlx` (direct) dẫn đến `mail.dlq`/`log.dlq`; không tự replay message lỗi.
6. Mail plain text UTF-8 lấy recipient và danh sách mã vé/event/venue từ DB; có timeout SMTP.
   `OrderMailService` chỉ làm nghiệp vụ đọc/map DTO, sender/processor/listener nằm ở `notification/`.
   PDF/QR chưa gửi, bổ sung ở Phase 7.
7. `ActivityLogService` dùng `REQUIRES_NEW`, khóa order và unique key chống ghi trùng.
   Mail có receipt + transaction riêng; crash sau SMTP nhưng trước DB commit vẫn có thể gửi trùng.
   Publish AFTER_COMMIT vẫn best effort, chưa có outbox/publisher confirms hay exactly-once.
8. Queue cũ không thêm arguments trực tiếp được: README hướng dẫn giữ declaration cũ bằng env
   `RABBITMQ_DECLARE_DEAD_LETTER_ARGUMENTS=false` kết hợp broker policy DLX tương đương.
   Không tự xóa/purge queue; không tắt arguments khi chưa có policy vì reject có thể làm mất message.

**Done ở mức code khi:** đủ consumer, schema, ACK/retry/DLQ, mail/log và compile không test thành công.
Checklist runtime gồm pay/cancel nhận đúng mail/log, replay idempotent, SMTP lỗi vào DLQ,
message sai JSON, consumer đồng thời và migrate queue cũ nằm trong README;
cần chạy trên MySQL/RabbitMQ/SMTP khi demo, không coi compile là xác nhận runtime.

---

## Phase 6 — Native query, Stored Procedure, EntityManager (`p6-reports`)

1. **Native query** (`@Query(nativeQuery=true)`): doanh thu theo ngày, top sự kiện bán chạy.
2. **Stored Procedure** MySQL `sp_revenue_report(IN p_from DATETIME, IN p_to DATETIME)` — tạo bằng changeset 014 (Liquibase `<sql>` hoặc `createProcedure`). Gọi qua `@Procedure` hoặc `entityManager.createStoredProcedureQuery(...)`.
3. **EntityManager**: một service báo cáo dùng `createNativeQuery`; một chỗ demo `persist/merge/flush/clear`.
4. Endpoint:
   - `GET /api/organizer/events/{id}/report`: doanh thu + số vé bán (owner).
   - `GET /api/admin/reports/revenue?from=&to=`: gọi stored procedure (ADMIN).

**Done khi:** 3 kiểu truy vấn chạy, report trả số liệu đúng.

---

## Phase 7 — Jasper: vé PDF có QR + báo cáo doanh thu PDF (`p7-jasper`)

1. Thêm `.jrxml` trong `src/main/resources/reports/`:
   - `ticket.jrxml`: Title + Detail + Image (QR) + Parameters (tên sự kiện, mã vé).
   - `revenue.jrxml`: Fields + band Summary tính tổng.
2. `util/QrCodeGenerator` (zxing) sinh ảnh QR từ `ticketCode`.
3. `service/report` compile + fill Jasper, export PDF (byte[]).
4. Endpoint:
   - `GET /api/tickets/{id}/pdf` (owner): vé PDF có QR.
   - `GET /api/organizer/events/{id}/report/pdf` (owner): báo cáo doanh thu PDF.
5. `MailListener` đính kèm vé PDF vào email.

**Done khi:** tải được PDF vé (QR quét ra ticketCode) và PDF báo cáo.

---

## Phase 8 — Dọn code & chuẩn bị demo (`p8-polish`)

- Rà soát quy ước AGENTS.md (indent tab, import nhóm, comment tiếng Việt giải thích "tại sao", DTO record, entity không setter).
- Bỏ comment thừa, kiểm tra build `./mvnw -q -DskipTests package`.
- Cập nhật README: bảng endpoint đầy đủ, biến môi trường (RabbitMQ, mail), hướng dẫn chạy MailHog.
- Git: mỗi phase một branch feature, commit message rõ ràng, không push thẳng main.

---

## Bản đồ đối chiếu kiến thức training

| Kiến thức | Thực hiện ở |
|---|---|
| Controller→Service→Repository, DTO record | Mọi phase |
| Status code 201/400/401/403/404/409 | Phase 4 (409 hết vé) |
| `@Configuration/@Bean` (PasswordEncoder, RabbitTemplate, JWT) | Phase 0, base |
| LAZY/EAGER, N+1, `@EntityGraph`, `@BatchSize` | Phase 3 |
| **Specification** + Pageable (thay JPQL) | Phase 3 |
| Native Query | Phase 6 |
| Stored Procedure (`@Procedure`) | Phase 6 |
| EntityManager (nativeQuery, persist/merge/flush/clear) | Phase 6 |
| Dirty checking | Phase 4 |
| `@Transactional` REQUIRED / REQUIRES_NEW / self-invocation | Phase 4 |
| RabbitMQ Topic, ACK, email bất đồng bộ | Phase 5 |
| Jasper (QR, Summary band) | Phase 7 |
| Git feature branch | Phase 8 |

> **Lưu ý test:** theo AGENTS.md, mặc định KHÔNG viết test khi develop; chỉ dùng compile/build để kiểm tra nhanh. Chỉ viết test khi được yêu cầu rõ ràng.
