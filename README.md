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
Tìm kiếm sự kiện public bằng **Specification** sẽ được triển khai ở Phase 3.

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
  Khi bỏ trống, Phase 4 sẽ áp dụng khoảng bán mặc định đến `startTime`.
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
