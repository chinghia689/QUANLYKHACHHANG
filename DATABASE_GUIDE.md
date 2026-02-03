# Hướng dẫn Quản lý Database (MySQL trên Docker)

Tài liệu này hướng dẫn cách cài đặt, truy cập và quản lý Database cho dự án **Customer Management System**.

## 1. Khởi chạy Database

Dự án sử dụng **Docker Compose** để chạy MySQL. Database được cấu hình trong file `docker-compose.yml`.

### Chạy lần đầu (Khởi tạo & Nạp dữ liệu mẫu)
Khi chạy lần đầu tiên, Docker sẽ tự động tạo database `quanlykhachhang` và chạy 2 file script sau:
1. `mysql-schema.sql`: Tạo bảng (`users`, `customers`).
2. `sample-users.sql`: Tạo 3 tài khoản mẫu (Admin, Manager, Staff).

```bash
# Tại thư mục gốc của dự án
docker-compose up -d
```
* `-d`: Chạy ngầm (detached mode).*

### Kiểm tra trạng thái
```bash
docker ps
```
Nếu thấy container tên `customer_app_db` đang `Up`, nghĩa là DB đang chạy ổn định.

---

## 2. Truy cập MySQL (Command Line)

Để gõ lệnh SQL trực tiếp, bạn cần chui vào bên trong container MySQL.

### Bước 1: Vào MySQL Shell
```bash
docker exec -it customer_app_db mysql -u root -D quanlykhachhang
```
*Bạn sẽ thấy dấu nhắc `mysql>` hiện ra.*

### Bước 2: Các lệnh kiểm tra dữ liệu

#### Xem danh sách bảng
```sql
SHOW TABLES;
```

#### Xem danh sách User (Tài khoản đăng nhập)
```sql
SELECT id, username, role, status, last_login FROM users;
```

#### Xem dữ liệu Khách hàng
```sql
SELECT * FROM customers;
```

#### Thoát ra
```sql
EXIT;
```

---

## 3. Quản lý Tài khoản (Reset Password)

Nếu quên mật khẩu Admin, bạn có thể reset lại bằng SQL. Mật khẩu trong DB được mã hóa BCrypt, nên không thể sửa trực tiếp thành text thường được.

### Cách Reset về mật khẩu mặc định: `admin123`
Hash của `admin123` là: `$2a$10$H4N4yotNv.6gBKSKMGa/WOajMmBnqhGCJWmgzelOdm9DVq80sglZu`

Chạy lệnh SQL sau trong MySQL Shell:
```sql
UPDATE users
SET password_hash = '$2a$10$H4N4yotNv.6gBKSKMGa/WOajMmBnqhGCJWmgzelOdm9DVq80sglZu',
    failed_attempts = 0,
    locked_until = NULL,
    status = 'ACTIVE'
WHERE username = 'admin';
```

---

## 4. Backup & Restore Dữ liệu

Dữ liệu MySQL được lưu trong thư mục `mysql_data/` ở máy thật (nhờ cấu hình Volume trong Docker).

### Backup (Sao lưu)
Để tạo file `.sql` chứa toàn bộ dữ liệu hiện tại:
```bash
docker exec customer_app_db mysqldump -u root quanlykhachhang > backup_data.sql
```

### Restore (Khôi phục)
Để nạp lại dữ liệu từ file backup:
```bash
docker exec -i customer_app_db mysql -u root quanlykhachhang < backup_data.sql
```

---

## 5. Reset Database về ban đầu (Xóa sạch làm lại)

Nếu DB bị lỗi hoặc bạn muốn xóa sạch dữ liệu để test lại từ đầu:

1. **Tắt container:**
   ```bash
   docker-compose down
   ```

2. **Xóa thư mục data cũ:**
   ```bash
   sudo rm -rf mysql_data
   ```
   *(Lệnh `sudo` cần thiết vì MySQL tạo file với quyền root)*

3. **Chạy lại (Docker sẽ tự tạo lại DB mới từ file sql gốc):**
   ```bash
   docker-compose up -d
   ```

---

## 6. Thông tin Kết nối (Dành cho Dev)
* **Host:** `localhost`
* **Port:** `3306`
* **Database:** `quanlykhachhang`
* **User:** `root`
* **Password:** *(rỗng)*
