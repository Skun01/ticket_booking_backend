# Backend Base Starter

Base project Spring Boot 4.1 backend dùng làm điểm khởi đầu sạch cho phát triển backend full stack.
Project tích hợp sẵn xác thực JWT HS256, xoay refresh token trong HttpOnly cookie, quản lý user và role (`ADMIN`, `ORGANIZER`, `CUSTOMER`), xử lý lỗi RFC 9457 ProblemDetail toàn cục, CORS cấu hình sẵn và migration Liquibase.

## Công nghệ

- Java 17, Spring Boot 4.1.1, Maven (`mvnw`)
- Spring Web MVC, Bean Validation
- Spring Data JPA, MySQL 8, Liquibase
- Spring Security + OAuth2 Resource Server (JWT HS256), BCrypt
- Springdoc OpenAPI (Swagger UI)
- Lombok
- Test: JUnit 5, MockMvc, Testcontainers (MySQL 8.4)

## Hướng dẫn chạy

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

- Mặc định chạy profile `dev` với URL: `jdbc:mysql://localhost:3306/base_project?createDatabaseIfNotExist=true`
- Tài khoản quản trị khởi tạo mặc định: `admin@example.com` / `admin12345`
- Tài liệu Swagger UI: `http://localhost:8080/swagger-ui.html`

## Các endpoint xác thực mặc định

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Đăng ký tài khoản (role `CUSTOMER`) |
| POST | `/api/auth/login` | Public | Đăng nhập tài khoản, trả Bearer access token và cấp cookie refresh token |
| POST | `/api/auth/refresh` | Public | Cấp mới access token và xoay refresh token từ cookie |
| POST | `/api/auth/logout` | Public | Thu hồi refresh token và xóa cookie |
| GET | `/api/users/me` | Authenticated | Lấy thông tin user đăng nhập hiện tại |

## API quản lý danh mục, địa điểm và sự kiện (Phase 2)

Các API dưới đây yêu cầu `Authorization: Bearer <access_token>`.

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET / POST | `/api/admin/categories` | ADMIN | Danh sách / tạo danh mục |
| GET / PUT / DELETE | `/api/admin/categories/{id}` | ADMIN | Chi tiết / sửa / xóa danh mục |
| GET / POST | `/api/admin/venues` | ADMIN | Danh sách / tạo địa điểm |
| GET / PUT / DELETE | `/api/admin/venues/{id}` | ADMIN | Chi tiết / sửa / xóa địa điểm |
| GET / POST | `/api/organizer/events` | ORGANIZER | Danh sách sự kiện của tôi / tạo sự kiện |
| GET / PUT | `/api/organizer/events/{id}` | ORGANIZER, owner | Chi tiết / cập nhật sự kiện |
| PATCH | `/api/organizer/events/{id}/status` | ORGANIZER, owner | Đổi trạng thái sự kiện |
| GET / POST | `/api/organizer/events/{id}/ticket-types` | ORGANIZER, owner | Danh sách / tạo hạng vé |
| PUT | `/api/ticket-types/{id}` | ORGANIZER, owner | Cập nhật hạng vé |

### Phân trang

Danh sách admin và danh sách sự kiện của organizer nhận `page` (mặc định `0`) và `size`
(mặc định `20`, tối đa `100`), sắp xếp `id` giảm dần. Response có các trường
`content`, `page`, `size`, `totalElements`, `totalPages`.
Tìm kiếm sự kiện public bằng **Specification** được mô tả ở phần Phase 3 bên dưới.

### Quy tắc nghiệp vụ

- Tạo sự kiện ở trạng thái `DRAFT`. Chỉ owner được truy cập các API quản lý sự kiện và hạng vé của mình.
- Chỉ sửa cấu hình sự kiện/hạng vé khi sự kiện còn `DRAFT`.
- Cho phép `DRAFT → PUBLISHED`, `DRAFT/PUBLISHED → CANCELLED`, `PUBLISHED → ENDED`.
  Không chuyển về `DRAFT`, không mở lại sự kiện đã hủy/kết thúc.
- Để publish: sự kiện chưa bắt đầu và có ít nhất một hạng vé. Chỉ kết thúc khi đã qua `endTime`.
- `endTime > startTime`; thời gian dùng ISO-8601 UTC, ví dụ `2027-06-01T12:00:00Z`.
- Giá vé không âm, tối đa 10 chữ số nguyên và 2 chữ số thập phân.
  `maxPerOrder <= totalQuantity`, tổng số vé mọi hạng không vượt sức chứa địa điểm.
- `saleStart`/`saleEnd` có thể bỏ trống; nếu có, khoảng bán phải hợp lệ và không vượt thời điểm bắt đầu sự kiện.
  Khi bỏ trống, khoảng bán mặc định kết thúc tại `startTime`.
- Không xóa danh mục/địa điểm đang được sự kiện sử dụng (`409`). Không đổi sức chứa địa điểm đã gắn sự kiện.
- `POST` thành công trả `201`, xóa trả `204`, dữ liệu sai trả `400`, không đúng role/owner trả `403`,
  không tìm thấy trả `404`, slug trùng hoặc xung đột cấu hình trả `409` (ProblemDetail có `code`).

### Ví dụ tạo dữ liệu

Admin tạo danh mục:

```json
{"name":"Âm nhạc","slug":"am-nhac"}
```

Admin tạo địa điểm:

```json
{"name":"Nhà thi đấu A","address":"123 Nguyễn Trãi","city":"Hồ Chí Minh","capacity":1000}
```

Organizer tạo sự kiện (dùng ID danh mục/địa điểm thực tế):

```json
{
  "categoryId":1,
  "venueId":1,
  "title":"Đêm nhạc mùa hè",
  "description":"Chương trình biểu diễn trực tiếp",
  "bannerUrl":null,
  "startTime":"2027-06-01T12:00:00Z",
  "endTime":"2027-06-01T15:00:00Z"
}
```

Organizer tạo hạng vé trước khi publish:

```json
{
  "name":"Standard",
  "price":250000.00,
  "totalQuantity":500,
  "maxPerOrder":5,
  "saleStart":"2027-05-01T00:00:00Z",
  "saleEnd":"2027-06-01T11:00:00Z"
}
```

Publish qua `PATCH /api/organizer/events/{id}/status` với body `{"status":"PUBLISHED"}`.
Đây là luồng publish trực tiếp theo Phase 2; bước admin duyệt sự kiện riêng chưa có trong phase này.

Hiện chưa có API cấp role ORGANIZER. Để demo trên DB development: đăng ký tài khoản CUSTOMER,
đổi role của đúng tài khoản đó thành ORGANIZER trong DB, rồi đăng nhập lại để nhận JWT mới.
Không cho client tự chọn role khi đăng ký.

## API public xem sự kiện (Phase 3)

Không cần JWT cho đúng hai endpoint GET sau. Các API quản lý và các HTTP method khác vẫn yêu cầu xác thực.
Nếu gửi kèm Bearer token không hợp lệ/hết hạn, Spring Security vẫn trả `401`.

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/events` | Lọc và phân trang sự kiện `PUBLISHED` |
| GET | `/api/events/{id}` | Chi tiết sự kiện `PUBLISHED`, kèm danh sách hạng vé |

### Bộ lọc và phân trang

| Query param | Quy tắc |
|---|---|
| `categoryId` | ID danh mục, số dương; không có danh mục tương ứng thì danh sách rỗng |
| `city` | So khớp tên thành phố đầy đủ, bỏ khoảng trắng hai đầu và không phân biệt hoa/thường; rỗng thì bỏ qua |
| `from` | `startTime >= from`, thời gian ISO-8601 có timezone |
| `to` | `startTime < to`, thời gian ISO-8601 có timezone |
| `page` | Mặc định `0`, không âm |
| `size` | Mặc định `20`, từ `1` đến `100` |

Các bộ lọc có thể kết hợp và luôn có điều kiện `status = PUBLISHED`.
Nếu truyền cả `from` và `to`, phải có `from < to`; sai khoảng trả `400`, code `INVALID_EVENT_FILTER_TIME`.
Kết quả sắp xếp `startTime ASC, id ASC` để phân trang ổn định khi nhiều sự kiện cùng giờ.
Không tự động lọc theo giờ hiện tại; sự kiện vẫn `PUBLISHED` thì vẫn xuất hiện cho đến khi đổi trạng thái.

Ví dụ xem sự kiện bắt đầu trong tháng 6/2027:

```bash
curl -G "http://localhost:8080/api/events" \
  --data-urlencode "categoryId=1" \
  --data-urlencode "city=Hồ Chí Minh" \
  --data-urlencode "from=2027-06-01T00:00:00Z" \
  --data-urlencode "to=2027-07-01T00:00:00Z" \
  --data-urlencode "page=0" \
  --data-urlencode "size=20"
```

Response phân trang gồm `content`, `page`, `size`, `totalElements`, `totalPages`.
Mỗi phần tử là DTO tóm tắt: `id`, `title`, `bannerUrl`, `startTime`, `endTime`, `category`, `venue`.
Danh sách không tải các hạng vé vì không cần cho DTO tóm tắt.

### Chi tiết và tồn vé

`GET /api/events/{id}` trả thêm `description` và `ticketTypes` (sắp xếp ID tăng dần).
Mỗi hạng vé có `availableQuantity = totalQuantity - soldQuantity`, giá và khoảng bán vé.
Hạng vé hết chỗ hoặc chưa mở bán vẫn được hiển thị; số còn lại không đồng nghĩa được phép đặt ngay.
Điều kiện đặt vé được xử lý ở Phase 4, tồn vé có thể thay đổi sau khi xem.
`soldQuantity` bao gồm cả vé đang giữ bởi đơn `PENDING` và vé đã thanh toán, không chỉ vé đã trả tiền.
ID không tồn tại hoặc sự kiện `DRAFT`/`CANCELLED`/`ENDED` đều trả `404`, code `EVENT_NOT_FOUND`.

### Specification và N+1

- `repository/specification/EventSpecifications` tạo từng điều kiện bằng Criteria API,
  service ghép bằng `Specification.allOf(...)`, repository thực thi `findAll(specification, pageable)`.
  Không dùng JPQL cho tìm kiếm/phân trang này.
- `@EntityGraph(category, venue)` tải hai quan hệ to-one cho danh sách và chi tiết.
  DTO được map bên trong service `@Transactional(readOnly = true)`, không phụ thuộc Open Session in View.
- `Event.ticketTypes` là collection LAZY có `@BatchSize(size = 50)`; chi tiết tải collection riêng.
  Batch loading hữu ích khi nhiều event trong cùng persistence context cần đọc collection.
  Không fetch join collection vào truy vấn phân trang, tránh nhân bản dòng/phân trang trong bộ nhớ.
- Theo thiết kế, danh sách cần một query dữ liệu và có thể thêm một query count; chi tiết cần query event
  (có category/venue) và query ticket types. Đây là số query dự kiến, chưa đo trên MySQL runtime.

Để quan sát khi demo local, bật `spring.jpa.show-sql=true` và
`spring.jpa.properties.hibernate.format_sql=true`, chuẩn bị nhiều sự kiện có category/venue khác nhau.
So sánh trên branch demo khi tạm bỏ EntityGraph: mapping DTO sẽ gây thêm query category/venue theo từng ID;
sau đó khôi phục EntityGraph. Với BatchSize, so sánh khi đọc ticketTypes của nhiều event trong cùng transaction.
Không giữ phiên bản gây N+1 trong luồng API chính.

## Đặt vé, thanh toán giả lập và check-in (Phase 4)

Liquibase thêm changeset **009–012**: `orders`, `order_items`, `tickets`, `payments`.
Các API yêu cầu Bearer JWT; danh sách dùng `PageResponse`, `page=0`, `size=20` (tối đa 100).

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/orders` | CUSTOMER | Giữ chỗ, tạo đơn `PENDING`, trả `201` |
| GET | `/api/orders/my` | CUSTOMER | Đơn của tôi, `createdAt DESC, id DESC` |
| GET | `/api/orders/{id}` | CUSTOMER, owner | Chi tiết đơn và giá vé đã chốt |
| POST | `/api/orders/{id}/pay` | CUSTOMER, owner | Thanh toán **MOCK**, sinh vé |
| POST | `/api/orders/{id}/cancel` | CUSTOMER, owner | Hủy đơn chưa thanh toán và trả kho |
| GET | `/api/tickets/my` | CUSTOMER | Vé của tôi, `id DESC` |
| POST | `/api/organizer/checkin` | ORGANIZER, owner event | Check-in bằng mã vé |

### Đặt vé và giữ chỗ

```json
{"ticketTypeId":1,"quantity":2}
```

- Mỗi đơn hiện mua **một hạng vé**, `quantity` từ `1` đến `100` và không vượt `maxPerOrder`.
- Tài khoản phải `ACTIVE`; event phải `PUBLISHED`, chưa đến `startTime`.
  Khoảng bán vé là `[saleStart, saleEnd)`; thiếu `saleStart` thì không giới hạn đầu,
  thiếu `saleEnd` thì vẫn phải mua trước `startTime`.
- Giá lấy từ DB và chốt vào `OrderItem.unitPrice`; client không quyết định giá/tổng tiền.
  Chưa triển khai mã giảm giá: `promoCode` dự phòng, `discountAmount = 0`.
- Conditional native UPDATE tăng `sold_quantity` nếu `quantity <= total_quantity - sold_quantity`.
  Không đủ vé trả `409 / SOLD_OUT`; lỗi tạo đơn sau giữ kho sẽ rollback luôn lượng giữ.
- `expiresAt = min(now + app.order.hold-minutes, startTime)`, mặc định giữ 15 phút.
  Env `ORDER_HOLD_MINUTES` phải dương. `soldQuantity` tính cả giữ chỗ lẫn đã thanh toán;
  thanh toán không trừ kho lần nữa, cancel/expire mới giảm `soldQuantity`.
- Create chưa có idempotency key: gửi lại POST create sẽ tạo đơn mới nếu còn chỗ.

### Thanh toán và retry

Không truyền body khi pay thì mặc định MOCK thành công. Hoặc gửi:

```json
{"method":"MOCK","simulateFailure":false}
```

- **Chỉ là cổng giả lập cho demo**, không xác thực giao dịch/thẻ/ví ngoài hệ thống; không dùng như cổng trả tiền thật.
- Thành công: `PAID`, một `Payment(SUCCESS)`, mỗi vé mua sinh một `Ticket(VALID)` với mã UUID ngẫu nhiên.
  Đơn, payment và tickets commit/rollback cùng nhau; xem mã vé qua `/api/tickets/my`.
- Pay lại đơn `PAID` trả lại đơn hiện tại, không tạo thêm payment, ticket hoặc message.
- Gửi `simulateFailure=true`: trả `402 / PAYMENT_DECLINED`, transaction thanh toán rollback,
  đơn vẫn `PENDING`, giữ chỗ không bị mất và không sinh ticket/message.
  Một `Payment(FAILED)` được lưu bằng transaction `REQUIRES_NEW` riêng; `paidAt` để trống.
- Đơn hết hạn trả `409 / ORDER_EXPIRED`; nếu còn `PENDING`, API chuyển `EXPIRED` và trả kho trước khi trả lỗi.
  Khoảng giữ chỗ có thể vượt `saleEnd`: đơn đã giữ hợp lệ vẫn được pay trước `expiresAt` nếu event còn mở.
- Đơn `CANCELLED` không pay được; event bị hủy/kết thúc thì không nhận thanh toán mới.

### Hủy, hết hạn và check-in

- Cancel đơn `PENDING` trả kho và chuyển `CANCELLED`; nếu đã tới hạn thì chuyển `EXPIRED` thay vì `CANCELLED`.
  Gọi lại cancel cho `CANCELLED`/`EXPIRED` trả trạng thái hiện tại, không trả kho lần nữa.
  Không cho cancel đơn `PAID`; chưa có refund hoặc hủy vé sau thanh toán.
- `OrderExpiryJob` chạy mỗi 60 giây, lấy tối đa 100 đơn đến hạn mỗi lượt, mỗi đơn trong transaction riêng.
  Khóa đơn rồi kiểm tra lại `PENDING` và `expiresAt <= now`; đơn vừa pay/cancel sẽ được bỏ qua.
  Nhiều instance có thể chọn trùng ứng viên nhưng khóa + kiểm tra lại ngăn trả kho hai lần.
  GET trả trạng thái đã lưu; đơn đến hạn có thể còn `PENDING` tới khi job hoặc pay/cancel xử lý, backlog có thể lâu hơn.
- Organizer check-in:

```json
{"ticketCode":"TKT-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}
```

  Chỉ owner event được check-in; ticket phải `VALID`, đơn `PAID`, event `PUBLISHED` và chưa qua `endTime`.
  Cho phép check-in trước `startTime`; thành công chuyển `USED` và ghi `checkedInAt`.
  Quét lại trả `409 / TICKET_ALREADY_USED`. Event đã hủy/kết thúc không được check-in.
  Hủy event không tự refund/hủy các đơn đã thanh toán trong phase này.

### Transaction và chống xử lý trùng

- Pay/cancel/expire dùng khóa `PESSIMISTIC_WRITE` trên cùng hàng order.
  Check-in khóa hàng ticket; publish/cấu hình event dùng khóa ghi, booking/pay/check-in dùng khóa chia sẻ event.
  Giữ kho vẫn dùng **conditional UPDATE**, không thay bằng phép đọc tồn rồi `save()`.
- Giữ thứ tự khóa event → ticket type ở cấu hình và booking để tránh khóa ngược chiều.
  Tạo đơn và cập nhật hạng vé dùng `READ_COMMITTED` khi đọc cấu hình sau khi chờ khóa event,
  tránh dùng snapshot cũ của MySQL `REPEATABLE_READ`.
- Không gọi `clearAutomatically` sau native stock update vì order còn cần dirty checking trong cùng transaction.
  Luồng này không sửa entity `TicketType` đã đọc nên không ghi đè bộ đếm kho bằng giá trị cũ.
- Dirty checking: `order.markPaid()/cancel()/expire()` và `ticket.use()` không cần gọi `save()` lại.
  `OrderExpiredException` riêng có `noRollbackFor` chỉ để commit thao tác expire + trả kho rồi trả `409`.
  Các lỗi stock, sinh vé, lưu payment khác vẫn rollback bình thường.
- `OrderPaymentService` không giữ transaction khi điều phối. Khi MOCK thất bại, transaction `OrderService.pay`
  phải rollback và nhả khóa order trước, rồi mới gọi bean `PaymentAuditService` với `REQUIRES_NEW`.
  Nếu insert FAILED trong transaction con khi cha còn khóa order, kiểm tra FK có thể chờ khóa cha.
- Job gọi `OrderService.expire()` qua bean Spring khác để mỗi đơn có transaction riêng;
  không gọi `this.expire()` rồi trông chờ `@Transactional`.
  Demo self-invocation chỉ làm trên branch demo: chuyển `expire()` vào chính job, gọi nội bộ rồi quan sát
  `TransactionSynchronizationManager.isActualTransactionActive()`; sau đó khôi phục bean riêng.
  Không đưa phiên bản sai này vào API/job chính.

### RabbitMQ sau commit

Pay/cancel phát `OrderMessage` bất biến; `notification/OrderEventPublisher` nhận bằng
`@TransactionalEventListener(AFTER_COMMIT)` và gửi `order.paid`/`order.cancelled` vào exchange đã cấu hình.
Message có `messageId`, `orderId`, `orderCode`, `userId`, `email`, `status`, `totalAmount`, `occurredAt`.
Đơn expire không phát `order.cancelled`; rollback/retry idempotent không phát thêm message.

Đây là publish sau commit **best effort**, chưa có transactional outbox/publisher confirm/retry bền vững:
DB có thể commit nhưng broker lỗi hoặc process dừng trước khi gửi. Lỗi gửi được log cùng orderId/messageId,
không biến pay/cancel đã commit thành HTTP lỗi giả. Chưa bảo đảm exactly-once hay chống mất message.
Consumer mail/activity log được bổ sung ở Phase 5 bên dưới; giới hạn publish best effort vẫn giữ nguyên.

### Kiểm chứng thủ công khi chạy demo

Sau khi có MySQL/RabbitMQ và chạy app để Liquibase tạo schema:
1. Chuẩn bị category, venue, event và hạng vé còn bán; publish event, dùng JWT CUSTOMER/ORGANIZER tương ứng.
2. Tạo đơn rồi pay; đối chiếu `payments`, số ticket đúng bằng quantity; pay lại phải không tăng số bản ghi.
3. Tạo đơn khác, thử MOCK thất bại; kiểm tra FAILED vẫn có nhưng order còn PENDING, không có ticket.
4. Thử cancel hai lần; `sold_quantity` chỉ giảm một lần. Với đơn đến hạn, chạy job hoặc gọi pay/cancel.
5. Với hạng vé chỉ còn một chỗ, gọi create đồng thời từ hai khách: kỳ vọng một `201`, một `409 SOLD_OUT`.
6. Gọi pay/cancel/job đồng thời cho cùng đơn: kỳ vọng chỉ một trạng thái cuối, kho không bị giảm hai lần.
7. Check-in đồng thời một mã vé: kỳ vọng một thành công, một `409 TICKET_ALREADY_USED`.

Các bước trên là checklist runtime, chưa được xác nhận chỉ bằng Maven compile; không tự tạo/chạy test.

## RabbitMQ: email vé và activity log (Phase 5)

Không thêm HTTP endpoint trong phase này. File Liquibase **013-create-notification-tables.yaml** thêm:
- `activity_logs`: userId, action (`ORDER_PAID`/`ORDER_CANCELLED`), entityType `ORDER`, entityId, detail,
  messageId, createdAt/updatedAt. Unique `(message_id, action)` chống ghi log trùng khi broker giao lại message.
  `detail` lưu thời điểm phát sự kiện `occurredAt`; createdAt là lúc consumer ghi nhận.
- `mail_deliveries`: receipt email với `order_id` unique, messageId và sentAt; chặn hai consumer cùng gửi một đơn.
  Receipt là thông tin xử lý kỹ thuật, không thay thế activity log nghiệp vụ.

### Topology mặc định

| Exchange | Routing key/binding | Queue | Xử lý |
|---|---|---|---|
| `order.exchange` (topic, durable) | `order.paid` | `mail.queue` (durable) | Email vé cho đơn PAID |
| `order.exchange` | `order.#` | `log.queue` (durable) | Log PAID/CANCELLED |
| `order.dlx` (direct, durable) | `mail.dlq` | `mail.dlq` (durable) | Message mail bị reject |
| `order.dlx` | `log.dlq` | `log.dlq` (durable) | Message log bị reject |

`MailListener`/`ActivityLogListener` dùng factory riêng, `MANUAL ACK`, prefetch mặc định 10.
Converter JSON chỉ trust package DTO order. Cả hai kiểm tra dữ liệu đầu vào, status/routing key và messageId;
AMQP messageId nếu có phải khớp messageId trong JSON. Consumer còn đối chiếu orderCode, userId, totalAmount,
status với DB, không dùng dữ liệu message để thay đổi trạng thái đơn.
Binding `order.#` chuẩn bị cho mở rộng nhưng hiện chỉ hỗ trợ PAID/CANCELLED; loại sự kiện khác vào DLQ.

### ACK, retry và giao lại message

- Activity log gọi bean `ActivityLogService` với `REQUIRES_NEW`, khóa order rồi kiểm tra unique key.
  Transaction đã commit mới `basicAck(tag, false)`; giao lại cùng messageId/action thì bỏ qua ghi trùng và ACK.
- Email nạp recipient, tên khách, thông tin sự kiện/venue và từng mã vé từ DB trong service transaction.
  Trường email trong message không được dùng làm đích gửi. DTO được map trước khi ra khỏi service.
- `OrderMailProcessor` có transaction riêng, upsert/khóa receipt theo orderId để serialize gửi email.
  Chỉ đánh dấu sentAt sau khi SMTP chấp nhận lệnh gửi và commit DB trước khi ACK.
  Receipt đã sent thì không gửi lại, kể cả replay đổi messageId; không khóa ghi order suốt lúc gọi SMTP.
- Retry chỉ bao quanh nghiệp vụ, không bao quanh ACK/reject: mặc định **2 retry + lần đầu = tối đa 3 lần**,
  nghỉ 1 giây rồi 2 giây. Mỗi lần retry gọi qua bean transaction mới, không tiếp tục transaction đã rollback.
  Message không hợp lệ không retry; JSON lỗi trước khi vào listener cũng bị reject trong MANUAL mode.
- Hết lượt: `basicReject(tag, false)`, broker chuyển sang DLX/DLQ. Không tự requeue vô hạn hay tự replay DLQ.
  Nếu kết nối/channel mất trước ACK, broker có thể giao lại; kiểm tra trùng bằng DB vẫn cần thiết.
- SMTP và MySQL không có transaction chung: crash/timeout sau SMTP chấp nhận nhưng trước DB commit có thể gửi trùng.
  sentAt có nghĩa SMTP đã nhận, không bảo đảm email đã tới inbox. Chưa bảo đảm exactly-once.
- DLQ không thay thế transactional outbox/publisher confirms. Publish từ app sang broker vẫn best effort;
  cơ chế dead-letter mặc định của classic queue cũng không phải bảo đảm chống mất tuyệt đối khi broker sự cố.

Email là plain text UTF-8, có orderCode, tổng tiền, mã vé/hạng vé, event, venue và giờ UTC.
Riêng Phase 5 chưa có PDF/QR; Phase 7 bên dưới đã bổ sung attachment PDF cho từng vé, giữ nguyên phần text.

### Chạy RabbitMQ và SMTP demo local

File `compose.yaml` chỉ có RabbitMQ Management và MailHog; **không tạo MySQL**, dùng MySQL service đã có.
Cần Docker Desktop/Docker Engine đang chạy. Từ thư mục `backend`:

```bash
docker compose up -d
docker compose ps
```

- RabbitMQ: AMQP `localhost:5672`, UI `http://localhost:15672`, tài khoản `guest` / `guest`.
- MailHog: SMTP `localhost:1025`, UI `http://localhost:8025`, không cần auth/TLS.
- Các cổng chỉ bind `127.0.0.1`, credential trên chỉ dùng cho development local, không dùng khi deploy public.
- Cổng/tài khoản khớp mặc định `application.yaml`; nếu đã override bằng env hoặc `.env`, cần chỉnh cho khớp.
- RabbitMQ có healthcheck và named volume giữ dữ liệu qua lần recreate; MailHog lưu email trong bộ nhớ,
  email cũ không được giữ sau khi service restart. Nếu đã có container cùng tên/cổng, xử lý xung đột trước khi chạy.
- Backend vẫn chạy trên máy host bằng `.\mvnw.cmd spring-boot:run`, kết nối MySQL service đang có.
  Exchange/queue/binding được backend khai báo khi kết nối RabbitMQ, không cần tạo tay trong Compose.

Xem log hoặc dừng hai service (không xóa volume):

```bash
docker compose logs -f rabbitmq mailhog
docker compose down
```

Không dùng `docker compose down -v` nếu muốn giữ dữ liệu RabbitMQ.

| Env | Mặc định | Ý nghĩa |
|---|---|---|
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Broker |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | `guest` / `guest` | Tài khoản local |
| `RABBITMQ_EXCHANGE` | `order.exchange` | Topic exchange |
| `RABBITMQ_MAIL_QUEUE` / `RABBITMQ_LOG_QUEUE` | `mail.queue` / `log.queue` | Queue xử lý |
| `RABBITMQ_DEAD_LETTER_EXCHANGE` | `order.dlx` | Direct exchange lỗi |
| `RABBITMQ_MAIL_DLQ` / `RABBITMQ_LOG_DLQ` | `mail.dlq` / `log.dlq` | Queue lỗi |
| `RABBITMQ_CONSUMER_MAX_RETRIES` | `2` | Số retry, từ 0 đến 10 (không tính lần đầu) |
| `RABBITMQ_CONSUMER_RETRY_DELAY_MILLIS` | `1000` | Delay đầu, 100–60000 ms; delay tối đa gấp 2 |
| `RABBITMQ_CONSUMER_PREFETCH` | `10` | Prefetch mỗi consumer, từ 1 đến 1000 |
| `RABBITMQ_DECLARE_DEAD_LETTER_ARGUMENTS` | `true` | App khai báo x-dead-letter-* trên queue |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `1025` | SMTP |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | rỗng | SMTP credentials nếu cần |
| `MAIL_SMTP_AUTH` / `MAIL_SMTP_STARTTLS` | `false` / `false` | Cấu hình SMTP theo nhà cung cấp |
| `MAIL_FROM` | `no-reply@sportslot.local` | From hợp lệ, cấu hình qua app.mail-sender.from |
| `MAIL_CONNECTION_TIMEOUT` / `MAIL_READ_TIMEOUT` / `MAIL_WRITE_TIMEOUT` | `5000` | Timeout SMTP, đơn vị ms |

### Nâng cấp broker đã có queue từ Phase 0–4

RabbitMQ không cho redeclare queue cũ với arguments khác: thêm `x-dead-letter-*` có thể báo
**`406 PRECONDITION_FAILED / inequivalent arg`**. App không tự xóa/purge queue.

**Cách không xóa queue cũ:** áp dụng broker policy khi backend/consumer đang dừng, rồi cấu hình
`RABBITMQ_DECLARE_DEAD_LETTER_ARGUMENTS=false` để app giữ declaration cũ (không x-arguments).
Ví dụ dưới đây chạy bằng **Bash/Git Bash**, dùng đúng vhost và tên queue thực tế:

```bash
docker exec sportslot-rabbit rabbitmqctl set_policy --apply-to queues sportslot-mail-dlx '^mail[.]queue$' \
  '{"dead-letter-exchange":"order.dlx","dead-letter-routing-key":"mail.dlq"}'
docker exec sportslot-rabbit rabbitmqctl set_policy --apply-to queues sportslot-log-dlx '^log[.]queue$' \
  '{"dead-letter-exchange":"order.dlx","dead-letter-routing-key":"log.dlq"}'
```

Sau đó khởi động app và kiểm tra queue effective policy trong UI; app vẫn khai báo DLX, DLQ và bindings.
Nếu queue đã có arguments từ Phase 5 thì giữ chế độ `true`, không đổi sang declaration thiếu arguments.
Nếu có policy khác, cần hợp nhất đúng DLX/routing key thay vì ghi đè tùy tiện.

**Không tắt declare-dead-letter-arguments khi chưa cấu hình policy tương đương:** reject không có DLX sẽ bỏ message.
Với môi trường dev queue trống, cũng có thể dừng app, kiểm tra không còn ready/unacked message, rồi operator
chủ động xóa/recreate queue; không áp dụng cách này khi còn dữ liệu. Không có thao tác xóa broker tự động trong code.

### Checklist runtime và replay DLQ

1. Pay đơn mới: kỳ vọng một log ORDER_PAID, một mail trong MailHog và receipt sentAt khác null.
2. Cancel đơn PENDING: kỳ vọng ORDER_CANCELLED, không gửi email vé; pay/cancel idempotent không phát message mới.
3. Replay cùng payload/messageId lên exchange/routing key gốc: log không thêm bản ghi, email không gửi lại.
4. Dừng MailHog rồi pay đơn khác: log vẫn xử lý riêng; mail retry hữu hạn rồi vào mail.dlq.
   Khi SMTP trở lại, operator replay đúng message lỗi; gửi thành công mới có receipt sentAt.
5. Payload thiếu field/sai UUID/status/routing key hoặc JSON hỏng: kỳ vọng DLQ, không treo unacked vô hạn.
6. Thử hai consumer/instance xử lý trùng: kỳ vọng log/receipt DB ngăn tạo trùng; vẫn lưu ý cửa sổ SMTP nêu trên.

Khi replay, giữ nguyên JSON messageId và AMQP messageId (nếu có), giữ content_type `application/json`,
publish lên **order.exchange với routing key gốc** (`order.paid` hoặc `order.cancelled`), không publish thẳng queue.
Một order.paid sẽ đi vào cả hai queue; consumer đã thành công sẽ bỏ qua nhờ idempotency.
Bỏ type headers không rõ nguồn; converter suy luận OrderMessage từ chữ ký listener.
Không tự chuyển DLQ ngược lại queue bằng TTL vì poison message có thể lặp vô hạn.
Chỉ ACK/xóa bản DLQ sau khi xác nhận replay đã được broker nhận và theo dõi kết quả; chưa có API replay trong app.

Các mục trên là hướng dẫn vận hành cần xác minh với MySQL/RabbitMQ/SMTP thực tế, không phải kết quả của compile.

## Báo cáo doanh thu (Phase 6)

Chỉ bổ sung hai API theo plan, không thêm bảng, queue hay cơ chế chạy nền:

| Method | Endpoint | Quyền | Nội dung |
|---|---|---|---|
| GET | `/api/organizer/events/{id}/report` | ORGANIZER, owner | Tổng doanh thu, số đơn/vé đã thanh toán và doanh thu theo ngày của sự kiện |
| GET | `/api/admin/reports/revenue?from=&to=` | ADMIN | Doanh thu toàn hệ thống trong khoảng thời gian, theo ngày và top 10 sự kiện |

### Quy tắc tính số liệu

- Chỉ lấy payment `SUCCESS` thuộc order `PAID`. Payment `FAILED`, đơn PENDING/CANCELLED/EXPIRED không được cộng.
- Doanh thu lấy từ `payments.amount`, không lấy giá vé hiện tại. Vé bán lấy `order_items.quantity` của đơn đã trả tiền,
  không dùng `ticket_types.sold_quantity` vì bộ đếm này còn bao gồm đơn giữ chỗ PENDING.
- Vé đã check-in vẫn là vé bán; event đã ENDED/CANCELLED vẫn có doanh thu nếu đơn còn PAID.
  Hiện chưa có refund nên báo cáo không tự trừ doanh thu khi hủy event.
- Organizer xem toàn bộ thời gian của một event. Event không tồn tại trả `404 EVENT_NOT_FOUND`,
  không phải owner trả `403 EVENT_NOT_OWNED`.
- Admin bắt buộc có `from`, `to` dạng ISO-8601, ví dụ `2027-06-01T00:00:00Z`.
  Khoảng lọc theo **paidAt**, quy ước **[from, to)**; `from >= to` trả `400 INVALID_REPORT_RANGE`.
  Bind tham số `Instant` theo UTC nhất quán với dữ liệu lưu, không phụ thuộc múi giờ JVM.
- Ngày doanh thu là ngày UTC. Chỉ trả ngày có payment phù hợp, không chèn thêm ngày doanh thu 0.
  Khi không có dữ liệu: tổng số đơn/vé và doanh thu bằng 0, danh sách rỗng.
- Top 10 xếp theo số vé bán giảm dần, sau đó doanh thu giảm dần, eventId tăng dần để xử lý đồng hạng.
  Đây là top trong đúng khoảng `[from, to)`, không phải top theo ngày diễn ra sự kiện.
- Truy vấn JOIN theo mô hình hiện tại: **mỗi đơn chỉ có một hạng vé**, mỗi đơn thanh toán thành công một lần.
  Nếu sau này cho mua nhiều hạng vé trong một đơn, cần điều chỉnh cách phân bổ doanh thu trước khi tái dùng query.

### Ba cách truy vấn trong code

1. `PaymentRepository`: `@Query(nativeQuery = true)` cho doanh thu theo ngày của event và top 10 event.
   Interface projection chỉ dùng để đọc kết quả SQL; controller trả DTO record, không trả entity/proxy.
2. `ReportServiceImpl.getEventReport`: `EntityManager.createNativeQuery` tính tổng event,
   kiểm tra owner trước khi truy vấn số liệu.
3. `ReportServiceImpl.getRevenueReport`: `EntityManager.createStoredProcedureQuery` gọi
   `sp_revenue_report(IN p_from DATETIME(6), IN p_to DATETIME(6))`, map result set thành DTO theo ngày.
   Tổng doanh thu/số đơn admin được cộng từ kết quả procedure; danh sách top event dùng native repository.

Liquibase **`014-create-revenue-report-procedure.yaml`** chỉ tạo procedure và có rollback DROP PROCEDURE.
`splitStatements: false` giữ nguyên thân `BEGIN ... END`; không dùng lệnh client `DELIMITER` trong JDBC.
Tài khoản chạy migration cần quyền `CREATE ROUTINE`; tài khoản gọi API cần `EXECUTE` procedure cùng quyền đọc
các bảng liên quan. Cấp quyền trên đúng database khi triển khai, không tự cấp quyền bằng migration này.
Các API báo cáo chạy trong transaction `readOnly`, không tạo dữ liệu demo hoặc thay đổi đơn/vé.

### Ví dụ gọi API

```bash
curl -H 'Authorization: Bearer <organizer-token>' \
  'http://localhost:8080/api/organizer/events/1/report'

curl -G -H 'Authorization: Bearer <admin-token>' \
  --data-urlencode 'from=2027-06-01T00:00:00Z' \
  --data-urlencode 'to=2027-07-01T00:00:00Z' \
  'http://localhost:8080/api/admin/reports/revenue'
```

Ví dụ cấu trúc response admin (số liệu minh họa):

```json
{
  "from":"2027-06-01T00:00:00Z",
  "to":"2027-07-01T00:00:00Z",
  "paidOrders":1,
  "revenue":500000.00,
  "dailyRevenue":[{"date":"2027-06-15","paidOrders":1,"revenue":500000.00}],
  "topEvents":[{"eventId":1,"eventTitle":"Đêm nhạc mùa hè","ticketsSold":2,"revenue":500000.00}]
}
```

### Demo EntityManager persist / merge / flush / clear

Đây là đoạn demo để chạy thủ công trên branch demo/local với DB development, không gắn vào API báo cáo,
không thêm bean/bảng chỉ để minh họa. Trong đoạn dưới, `entityManager` và `transactionManager` là bean Spring
được inject; chạy ngoài transaction báo cáo và không dùng connection độc lập khác.

```java
String slug = "demo-em-" + UUID.randomUUID();
TransactionTemplate transaction = new TransactionTemplate(transactionManager);
transaction.executeWithoutResult(status -> {
	status.setRollbackOnly();
	Category category = new Category("Demo persist", slug);
	entityManager.persist(category);
	entityManager.flush();
	entityManager.clear();

	category.update("Demo merge", slug);
	Category managed = entityManager.merge(category);
	entityManager.flush();
	entityManager.clear();
});
```

Dùng các import: `java.util.UUID`, `com.example.base.domain.Category`,
`org.springframework.transaction.support.TransactionTemplate`.

- `persist`: đưa entity mới vào persistence context; với IDENTITY, INSERT có thể chạy ngay để lấy ID.
- `flush`: đẩy thay đổi xuống DB, **không phải commit**.
- `clear`: detach toàn bộ entity trong context; vì vậy flush trước để không làm mất thay đổi đang chờ.
- `merge`: copy trạng thái của entity detached vào entity managed và trả entity managed (`managed`);
  biến `category` cũ vẫn detached.
- Transaction được đánh dấu rollback từ đầu nên bản ghi demo không được giữ lại sau khi kết thúc.
  AUTO_INCREMENT vẫn có thể tăng dù rollback; không chạy demo này trên database production.

### Checklist khi chạy với MySQL

1. Chạy app để Liquibase tạo procedure 014; gọi report bằng role ADMIN/ORGANIZER tương ứng.
2. Tạo hai đơn: một pay thành công, một MOCK thất bại. Đối chiếu chỉ đơn thành công được cộng.
3. Tạo thêm đơn PENDING còn giữ kho: số vé bán trên report không tăng theo reservation.
4. So sánh tổng event với dailyRevenue; tổng admin với kết quả procedure gọi trực tiếp:
   `CALL sp_revenue_report('2027-06-01 00:00:00', '2027-07-01 00:00:00');`.
5. Đối chiếu paidAt đúng đầu/cuối khoảng: tính tại from, không tính tại to; thử khoảng không có dữ liệu.
6. Thử organizer không sở hữu event và CUSTOMER gọi API report: kỳ vọng bị từ chối.

Đây là checklist runtime cần thực hiện trên MySQL, chưa được xác nhận bằng compile hay kiểm tra tĩnh.
Không tự tạo/chạy test. PDF report được bổ sung ở Phase 7 bên dưới.

## Vé PDF có QR và báo cáo PDF (Phase 7)

Không thêm bảng, queue, filesystem lưu vé hay job. Hai API yêu cầu Bearer JWT:

| Method | Endpoint | Quyền | File tải xuống |
|---|---|---|---|
| GET | `/api/tickets/{id}/pdf` | CUSTOMER, owner đơn chứa vé | `ticket-{id}.pdf` |
| GET | `/api/organizer/events/{id}/report/pdf` | ORGANIZER, owner event | `event-{id}-revenue.pdf` |

Response thành công là `application/pdf`, `Content-Disposition: attachment`, có Content-Length và
`Cache-Control: no-store`. `{id}` của vé là ID số lấy từ `/api/tickets/my`, không phải chuỗi ticketCode.
Thiếu JWT/sai role vẫn bị chặn theo security hiện có. Không có endpoint public hay admin PDF mới.

### Nội dung và cách sinh file

- `reports/ticket.jrxml`: Title là tên sự kiện, Detail có hạng vé, thời gian UTC, venue/address,
  trạng thái tại thời điểm xuất, mã vé và ảnh QR từ Parameters.
- `util/QrCodeGenerator`: ZXing sinh ảnh QR 240x240, có quiet zone, error correction M.
  Nội dung QR là **đúng ticketCode**, không phải URL, JWT hay thông tin khách hàng.
- `reports/revenue.jrxml`: Fields ngày/số đơn/doanh thu; Detail liệt kê doanh thu ngày UTC;
  Summary dùng Jasper variables SUM để cộng số đơn và doanh thu. Header có tổng vé đã bán.
  Báo cáo rỗng vẫn có tiêu đề, tổng 0 và thông báo chưa có giao dịch.
- PDF report dùng lại dữ liệu và kiểm tra owner của `ReportService.getEventReport` ở Phase 6,
  xem toàn thời gian của event; không thêm bộ lọc hay cách tính doanh thu khác.
- `util/JasperPdfRenderer` compile hai template từ classpath một lần khi khởi tạo bean,
  sau đó mỗi lần fill dùng parameters/datasource riêng và export `byte[]` trong bộ nhớ.
  Tránh dùng Bean datasource trực tiếp với record: các dòng doanh thu được chuyển thành map.
- Font DejaVu Sans đi kèm dependency `jasperreports-fonts` cùng version Jasper, hỗ trợ và nhúng Unicode
  tiếng Việt trong PDF; không phụ thuộc font cài sẵn riêng trên máy Windows.
- `TicketService`/`ReportService` giữ kiểm tra quyền nghiệp vụ; phần kỹ thuật Jasper/QR nằm trong `util/`
  theo AGENTS, không tạo thêm layer hay service phụ chỉ để bọc renderer.

Vé không tồn tại trả `404 TICKET_NOT_FOUND`; vé của người khác trả `403 TICKET_NOT_OWNED`.
Lỗi event giữ `EVENT_NOT_FOUND`/`EVENT_NOT_OWNED` như API JSON. Lỗi sinh PDF/QR được xử lý qua
AppException (`PDF_GENERATION_FAILED`/`QR_GENERATION_FAILED`) và ProblemDetail.
Template thiếu/sai khi khởi động sẽ làm bean renderer khởi tạo thất bại, không âm thầm bỏ attachment.

Owner vẫn có thể tải vé USED hoặc vé của event đã kết thúc/hủy để giữ chứng từ. PDF là bản chụp thông tin,
không làm vé hợp lệ lại. Khi quét QR, organizer vẫn phải gọi API check-in; DB quyết định trạng thái thực tế,
không tin trạng thái đã in trên PDF. Không chia sẻ file chứa ticketCode cho người không liên quan.

### Email đính kèm vé

`MailListener` tiếp tục dùng luồng Phase 5; `OrderMailSender` tạo email multipart UTF-8,
phần text giữ nguyên, **mỗi vé một attachment `{ticketCode}.pdf`** bằng cùng renderer của API.
Tất cả attachment được chuẩn bị trước khi gọi SMTP. Nếu tạo PDF thất bại thì không gửi email thiếu vé;
exception đi theo retry/DLQ hiện có, receipt không được ghi sentAt thành công.
Giới hạn SMTP/DB không atomic và publish best effort từ Phase 5 không thay đổi.

Receipt đã có sentAt (kể cả email text cũ từ Phase 5) vẫn được bỏ qua khi replay; nâng cấp Phase 7 không tự
phát lại email đã gửi. Khách có thể tải PDF qua API. Không tự xóa receipt hoặc thêm API resend.
Đơn tối đa 100 vé nên email có thể có nhiều attachment; cần kiểm tra giới hạn dung lượng SMTP khi demo.

### Ví dụ tải file

```bash
curl --fail -H 'Authorization: Bearer <customer-token>' \
  'http://localhost:8080/api/tickets/1/pdf' -o ticket-1.pdf

curl --fail -H 'Authorization: Bearer <organizer-token>' \
  'http://localhost:8080/api/organizer/events/1/report/pdf' -o event-1-revenue.pdf
```

Thay ID/token bằng dữ liệu thực tế. `--fail` tránh lưu ProblemDetail lỗi vào file có đuôi `.pdf`.

### Checklist runtime Phase 7

1. Pay một đơn mới, tải PDF từ ticket ID trong danh sách vé; kiểm tra tiếng Việt, ngày UTC và venue.
2. Quét QR trong PDF: kết quả phải khớp ticketCode trong API; gọi check-in đúng organizer để xác minh vé.
3. Thử user khác tải vé, organizer khác tải report và ID không tồn tại: kiểm tra 403/404, không lộ PDF.
4. So sánh số liệu PDF report với API JSON của cùng event, gồm event chưa có doanh thu và báo cáo nhiều ngày.
5. Xem email mới trong MailHog: số attachment bằng số vé; mở từng PDF và đối chiếu từng mã QR.
6. Check-in rồi tải lại PDF: trạng thái là USED, không tạo vé mới và không cho check-in lần hai.
7. Replay message đã gửi thành công: không tự gửi lại email/PDF do receipt Phase 5.

Compile Java và compile JRXML là kiểm tra build; không chứng minh PDF đã render/mở đúng,
QR đã quét được, email đã gửi hoặc quyền API đã được kiểm chứng runtime. Không tự tạo/chạy test.
