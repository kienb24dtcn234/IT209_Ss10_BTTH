# Student API — Spring Boot + MySQL + Docker Compose

API quản lý sinh viên: CRUD, tìm kiếm, sắp xếp, phân trang. Dữ liệu lưu trong MySQL, cả 2 dịch vụ chạy bằng `docker compose`, biến môi trường tách sang `.env`.

## Công nghệ
- Java 17, Spring Boot 3.3.5 (Web, Data JPA, Validation)
- MySQL 8.0
- Docker multi-stage build + Docker Compose

## Cấu trúc
```
student-api/
├── src/main/java/com/ptit/student/
│   ├── StudentApiApplication.java
│   ├── controller/StudentController.java
│   ├── service/StudentService.java
│   ├── repository/StudentRepository.java
│   ├── entity/Student.java, Gender.java
│   ├── dto/StudentRequest.java, PageResponse.java
│   └── exception/GlobalExceptionHandler.java, NotFoundException.java, DuplicateException.java
├── src/main/resources/application.yml
├── Dockerfile
├── docker-compose.yml
├── .env / .env.example
└── README.md
```

## Đối tượng Student
| Trường | Kiểu | Ràng buộc |
|---|---|---|
| `id` | Long | Tự tăng |
| `studentCode` | String | Mã sinh viên, bắt buộc, không trùng, tối đa 20 ký tự |
| `fullName` | String | Tên sinh viên, bắt buộc, tối đa 100 ký tự |
| `dateOfBirth` | LocalDate | Ngày sinh `yyyy-MM-dd`, phải trong quá khứ |
| `gender` | Enum | `MALE`, `FEMALE`, `OTHER` |
| `phone` | String | 10 chữ số, bắt đầu bằng 0 |
| `gpa` | Double | 0.0 đến 4.0 |

## Biến môi trường (`.env`)
| Biến | Ý nghĩa |
|---|---|
| `MYSQL_ROOT_PASSWORD` | Mật khẩu root MySQL |
| `MYSQL_DATABASE` | Tên database |
| `MYSQL_USER` / `MYSQL_PASSWORD` | Tài khoản app dùng để kết nối |
| `MYSQL_HOST_PORT` | Cổng MySQL mở ra máy host (3307, tránh trùng MySQL cài sẵn) |
| `APP_HOST_PORT` | Cổng API mở ra máy host (8080) |

`docker-compose.yml` đọc `.env` và truyền vào app dưới dạng `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`. App kết nối MySQL qua tên service `mysql` trong network `student-net`.

## Chạy dự án
```bash
docker compose up -d --build
docker compose ps
docker compose logs -f app      # chờ thấy "Started StudentApiApplication", Ctrl+C để thoát
```
Dừng: `docker compose down` (thêm `-v` để xóa luôn dữ liệu MySQL).

## Danh sách API
Base URL: `http://localhost:8080/api/students`

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/students` | Danh sách + tìm kiếm + sắp xếp + phân trang |
| GET | `/api/students/{id}` | Xem chi tiết |
| POST | `/api/students` | Thêm mới |
| PUT | `/api/students/{id}` | Cập nhật |
| DELETE | `/api/students/{id}` | Xóa |

Tham số của `GET /api/students` (đều không bắt buộc):

| Tham số | Mặc định | Ý nghĩa |
|---|---|---|
| `keyword` | | Tìm theo tên, mã sinh viên hoặc số điện thoại |
| `gender` | | Lọc `MALE` / `FEMALE` / `OTHER` |
| `minGpa`, `maxGpa` | | Lọc khoảng GPA |
| `page` | `0` | Trang (bắt đầu từ 0) |
| `size` | `10` | Số bản ghi mỗi trang (tối đa 100) |
| `sortBy` | `id` | `id`, `studentCode`, `fullName`, `dateOfBirth`, `gender`, `gpa` |
| `sortDir` | `asc` | `asc` hoặc `desc` |

Mã lỗi: `400` dữ liệu không hợp lệ, `404` không tìm thấy, `409` trùng mã sinh viên.

## Test nhanh bằng curl
```bash
# Thêm mới
curl -s -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"studentCode":"B24DCCN001","fullName":"Nguyễn Văn An","dateOfBirth":"2006-03-15","gender":"MALE","phone":"0912345678","gpa":3.45}'

curl -s -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"studentCode":"B24DCCN002","fullName":"Trần Thị Bình","dateOfBirth":"2006-07-20","gender":"FEMALE","phone":"0987654321","gpa":3.80}'

curl -s -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"studentCode":"B24DCCN003","fullName":"Lê Minh Châu","dateOfBirth":"2005-11-02","gender":"MALE","phone":"0901112233","gpa":2.95}'

# Danh sách
curl -s "http://localhost:8080/api/students"

# Tìm kiếm + sắp xếp GPA giảm dần + phân trang
curl -s "http://localhost:8080/api/students?keyword=nguyen&sortBy=gpa&sortDir=desc&page=0&size=5"
curl -s "http://localhost:8080/api/students?gender=FEMALE&minGpa=3.5"

# Chi tiết, cập nhật, xóa
curl -s http://localhost:8080/api/students/1
curl -s -X PUT http://localhost:8080/api/students/1 -H "Content-Type: application/json" \
  -d '{"studentCode":"B24DCCN001","fullName":"Nguyễn Văn An","dateOfBirth":"2006-03-15","gender":"MALE","phone":"0912345678","gpa":3.60}'
curl -i -X DELETE http://localhost:8080/api/students/3
```

Lưu ý: tìm kiếm `keyword=nguyen` vẫn khớp "Nguyễn" vì MySQL dùng collation `utf8mb4_unicode_ci` (không phân biệt dấu và hoa thường).
