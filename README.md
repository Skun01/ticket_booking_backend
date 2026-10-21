# Backend Base Starter

Base project Spring Boot 4.1 backend dùng làm điểm khởi đầu sạch cho phát triển backend full stack.
Project tích hợp sẵn xác thực JWT HS256, xoay refresh token trong HttpOnly cookie, quản lý user và role (`USER`, `ADMIN`), xử lý lỗi RFC 9457 ProblemDetail toàn cục, CORS cấu hình sẵn và migration Liquibase.

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
| POST | `/api/auth/register` | Public | Đăng ký tài khoản (role `USER`) |
| POST | `/api/auth/login` | Public | Đăng nhập tài khoản, trả Bearer access token và cấp cookie refresh token |
| POST | `/api/auth/refresh` | Public | Cấp mới access token và xoay refresh token từ cookie |
| POST | `/api/auth/logout` | Public | Thu hồi refresh token và xóa cookie |
| GET | `/api/users/me` | Authenticated | Lấy thông tin user đăng nhập hiện tại |
