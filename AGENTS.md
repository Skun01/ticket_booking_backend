# AGENTS.md

Spring Boot 4.1 backend (Java 17, Maven, MySQL, Liquibase, Spring Security OAuth2 Resource Server với JWT HS256). Đọc `README.md` để biết tổng quan, endpoint, profile và biến môi trường.

File này có hai phần. **Phần BASE** là quy ước của base project, được giữ nguyên khi tạo project mới. **Phần PROJECT** do từng project con tự điền.

<!-- ==================== PHẦN BASE ==================== -->

## Lệnh

- Chạy app: `./mvnw spring-boot:run` (tự bật profile `dev`, cần MySQL ở localhost:3306). Trên Windows/PowerShell dùng `.\mvnw.cmd`.
- Test toàn bộ: `./mvnw test` (cần Docker cho Testcontainers)
- Một test class: `./mvnw test -Dtest=AuthControllerTests`
- Một test method: `./mvnw test -Dtest=AuthControllerTests#refreshRotatesRefreshToken`
- Không bật profile nào thì app không khởi động (thiếu datasource). Đây là chủ ý, không thêm profile mặc định.

## Kiến trúc

- Package theo layer: `com.example.base.<layer>`, không chia package theo feature. Tên layer dùng số ít: `controller`, `service`, `domain`, `repository`, `dto`, `mapper`, `exception`, `config`, `job`, `notification`, `util`. Chỉ tạo layer khi cần.
- `service/` chỉ chứa interface và class nghiệp vụ thuần túy (business service). Triển khai nằm trong `service/impl/`. Không đặt DTO nội bộ, message listener, notification sender hoặc helper kỹ thuật trong `service/`.
- `notification/` chứa các thành phần gửi thông báo và hàng đợi messaging.
- `util/` chứa các bean tiện ích, thuật toán hoặc strategy kỹ thuật dùng chung.
- Hạ tầng đặt trong layer tương ứng: security, CORS, scheduling và cấu hình khởi tạo ở `config/`; `BaseEntity` ở `domain/`; xử lý lỗi ở `exception/`.
- Controller gọi service, service truy cập repository theo trách nhiệm nghiệp vụ.
- **Luồng xác thực:** access token là JWT do `TokenService` ký. Claim `sub` là id user, claim `roles` được `SecurityConfig` map thành `ROLE_*`. Trong controller, lấy user hiện tại bằng `@AuthenticationPrincipal Jwt jwt` + `Long.valueOf(jwt.getSubject())`. Refresh token là chuỗi ngẫu nhiên nằm trong cookie, DB chỉ lưu hash SHA-256, token được rotate mỗi lần refresh.
- **Luồng lỗi:** mọi lỗi đều đi qua `GlobalExceptionHandler` và được trả về dạng ProblemDetail có thêm field `code`.
- `spring.jpa.open-in-view: false`: lazy loading chỉ chạy được bên trong transaction của service, nên map entity sang DTO trước khi trả về controller.
- Thời gian lưu bằng `Instant`, Hibernate dùng timezone UTC.

## Quy ước code

- Entity không có setter, đổi trạng thái qua method nghiệp vụ. Không dùng `@Data`/`@Setter`/`@Builder`.
- Controller chỉ trả DTO `record`. Map bằng `Response.from(entity)`, chỉ tạo `mapper/` khi map phức tạp.
- Lỗi: `throw new AppException(<Feature>ErrorCode.X)` với enum riêng của feature. Không dùng `ResponseStatusException`.
- Các lớp `*Properties` đặt trong `config/property/` (package `com.example.base.config.property`).
- DTO được nhóm theo domain trong sub-package tương ứng (`dto/auth`, `dto/common`...).
- Config: đặt dưới `app.*`, đọc bằng `@ConfigurationProperties` record, không dùng `@Value`.
- Comment bằng tiếng Việt có dấu, đặt ở dòng riêng, chỉ giải thích "tại sao".
- Swagger: chỉ viết summary ngắn gọn qua `@Operation(summary = "...")` cho các API để quá trình develop dễ nhận diện API đấy để làm gì, không viết chi tiết rườm rà.

## Format code

- Java indent bằng tab. YAML indent 2 dấu cách. Dòng tối đa 120 ký tự.
- Xuống dòng giữa câu lệnh: thụt 2 tab, không dùng dấu cách.
- `else`/`catch`/`finally` xuống dòng mới, không viết chung dòng với `}`.
- Có dòng trống sau dòng khai báo class và trước `}` đóng class. Có dòng trống trước `return` nếu method có nhiều câu lệnh.
- Import chia nhóm, cách nhau một dòng trống: `java.*` → thư viện khác → `org.springframework.*` → `static`. Không dùng wildcard.

## Database

- Schema chỉ thay đổi qua Liquibase: thêm file mới `src/main/resources/db/changelog/changes/NNN-mo-ta.yaml` (NNN tăng dần) và include vào `db.changelog-master.yaml`.
- Không bao giờ sửa changeset đã có.
- `ddl-auto: validate`: entity phải khớp schema, nếu không app không khởi động.

## Bảo mật

- Mọi endpoint mặc định yêu cầu JWT. Chỉ `/api/auth/**`, `/error` và Swagger UI / OpenAPI (`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`) là public.
- Phân quyền bằng `@PreAuthorize("hasRole('ADMIN')")`.
- Không commit secret thật. Profile `prod` lấy mọi giá trị nhạy cảm từ biến môi trường.

<!-- ==================== PHẦN PROJECT ==================== -->

## Cấu trúc service

- `service/` chứa toàn bộ interface nghiệp vụ (`AuthService`, `TokenService`, `UserService`...).
- Các lớp triển khai service nằm trong `service/impl`, có hậu tố `Impl`; đặt `@Service` và các annotation `@Transactional` trên phần triển khai, không trên interface.
- Controller và các lớp sử dụng service inject qua interface.
