# 🔄 Cập Nhật: Chuyển Đổi Từ SQLite Sang MySQL

## ✅ Các Thay Đổi Đã Thực Hiện

### 1. **Maven Dependencies** ([pom.xml](file:///home/chinghia/Java/customer-management-app/pom.xml))

**Trước (SQLite):**
```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.0.0</version>
</dependency>
```

**Sau (MySQL):**
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

---

### 2. **Database Connection** ([DatabaseManager.java](file:///home/chinghia/Java/customer-management-app/src/main/java/com/customer/dao/DatabaseManager.java))

**Cấu hình kết nối MySQL:**
```java
private static final String DB_HOST = "localhost";
private static final String DB_PORT = "3306";
private static final String DB_NAME = "customer_management";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = ""; // XAMPP mặc định
```

**URL kết nối:**
```java
jdbc:mysql://localhost:3306/customer_management?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
```

**Features:**
- ✅ Tự động tạo database nếu chưa tồn tại
- ✅ Load MySQL JDBC driver tự động
- ✅ Thông báo rõ ràng khi kết nối thành công/thất bại
- ✅ Hướng dẫn nếu MySQL chưa chạy

---

### 3. **Database Schema** ([schema.sql](file:///home/chinghia/Java/customer-management-app/src/main/resources/database/schema.sql))

**Thay đổi từ SQLite sang MySQL syntax:**

| Thay Đổi | SQLite | MySQL |
|----------|--------|-------|
| **ID Column** | `INTEGER PRIMARY KEY AUTOINCREMENT` | `BIGINT PRIMARY KEY AUTO_INCREMENT` |
| **Text Fields** | `TEXT` | `VARCHAR(255)`, `TEXT` |
| **Date/Time** | `TEXT` | `DATE`, `DATETIME` |
| **Engine** | N/A | `ENGINE=InnoDB` |
| **Charset** | N/A | `CHARSET=utf8mb4` |
| **Indexes** | Separate statements | Inline in CREATE TABLE |

**Schema mới:**
```sql
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    date_of_birth DATE,
    customer_type VARCHAR(20) NOT NULL,
    created_date DATETIME NOT NULL,
    INDEX idx_full_name (full_name),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_customer_type (customer_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 🚀 Cách Chạy Ứng Dụng Với MySQL

### Bước 1: Cài Đặt MySQL

**Option A: MySQL Standalone (Đơn giản hơn)**
```bash
sudo apt update
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql
```

**Option B: XAMPP (Có GUI phpMyAdmin)**
```bash
# Xem chi tiết trong MYSQL_SETUP.md
wget https://sourceforge.net/projects/xampp/files/XAMPP%20Linux/8.2.12/xampp-linux-x64-8.2.12-0-installer.run
chmod +x xampp-linux-x64-8.2.12-0-installer.run
sudo ./xampp-linux-x64-8.2.12-0-installer.run
```

### Bước 2: Khởi Động MySQL

**Với MySQL Standalone:**
```bash
sudo systemctl start mysql
sudo systemctl status mysql  # Kiểm tra
```

**Với XAMPP:**
```bash
sudo /opt/lampp/lampp startmysql
```

### Bước 3: Compile & Run

```bash
cd /home/chinghia/Java/customer-management-app

# Compile
mvn clean compile

# Run
mvn javafx:run
```

---

## 📊 Ưu Điểm MySQL So Với SQLite

### ✅ **Performance**
- Tốt hơn cho lượng data lớn
- Query optimization tốt hơn
- Connection pooling

### ✅ **Scalability**
- Hỗ trợ nhiều kết nối đồng thời
- Phù hợp client-server architecture
- Có thể deploy trên server riêng

### ✅ **Features**
- Stored procedures
- Triggers
- Views
- User management
- Replication

### ✅ **Tools**
- phpMyAdmin (GUI quản lý)
- MySQL Workbench
- Command-line tools
- Backup/Restore dễ dàng

---

## 🔧 Cấu Hình Tùy Chỉnh

### Thay Đổi Thông Tin Kết Nối

Sửa trong [DatabaseManager.java](file:///home/chinghia/Java/customer-management-app/src/main/java/com/customer/dao/DatabaseManager.java):

```java
// Thay đổi host (nếu MySQL ở máy khác)
private static final String DB_HOST = "192.168.1.100";

// Thay đổi port (nếu không dùng port mặc định)
private static final String DB_PORT = "3307";

// Thay đổi tên database
private static final String DB_NAME = "my_custom_db";

// Thay đổi user/password
private static final String DB_USER = "myuser";
private static final String DB_PASSWORD = "mypassword";
```

### Tạo User Riêng (Bảo Mật Tốt Hơn)

```sql
-- Đăng nhập MySQL
mysql -u root -p

-- Tạo user mới
CREATE USER 'customer_app'@'localhost' IDENTIFIED BY 'secure_password';

-- Cấp quyền
GRANT ALL PRIVILEGES ON customer_management.* TO 'customer_app'@'localhost';
FLUSH PRIVILEGES;
```

Sau đó update trong `DatabaseManager.java`:
```java
private static final String DB_USER = "customer_app";
private static final String DB_PASSWORD = "secure_password";
```

---

## 🗄️ Quản Lý Database

### Xem Dữ Liệu Qua Command Line

```bash
# Kết nối MySQL
mysql -u root -p

# Chọn database
USE customer_management;

# Xem tất cả tables
SHOW TABLES;

# Xem cấu trúc bảng
DESCRIBE customers;

# Xem dữ liệu
SELECT * FROM customers;

# Đếm số khách hàng
SELECT COUNT(*) FROM customers;

# Lọc theo loại
SELECT * FROM customers WHERE customer_type = 'VIP';
```

### Xem Qua phpMyAdmin (nếu dùng XAMPP)

1. Mở browser: http://localhost/phpmyadmin
2. Login: user `root`, password (empty hoặc theo cấu hình)
3. Chọn database `customer_management`
4. Click vào table `customers`

---

## 🔄 Backup & Restore

### Backup Database

```bash
# Backup toàn bộ database
mysqldump -u root -p customer_management > backup_$(date +%Y%m%d).sql

# Backup chỉ structure (không có data)
mysqldump -u root -p --no-data customer_management > structure.sql

# Backup chỉ data
mysqldump -u root -p --no-create-info customer_management > data.sql
```

### Restore Database

```bash
# Restore từ file backup
mysql -u root -p customer_management < backup_20260123.sql

# Hoặc import trong MySQL
mysql -u root -p
USE customer_management;
SOURCE /path/to/backup.sql;
```

---

## ⚠️ Xử Lý Lỗi Thường Gặp

### Lỗi 1: "Can't connect to MySQL server on 'localhost'"

**Nguyên nhân:** MySQL chưa chạy

**Giải pháp:**
```bash
# Kiểm tra status
sudo systemctl status mysql

# Khởi động
sudo systemctl start mysql
```

### Lỗi 2: "Access denied for user 'root'@'localhost'"

**Nguyên nhân:** Sai password

**Giải pháp:**
```bash
# Reset root password
sudo mysql

ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';
FLUSH PRIVILEGES;
EXIT;
```

### Lỗi 3: "Unknown database 'customer_management'"

**Nguyên nhân:** Database chưa được tạo

**Giải pháp:** Ứng dụng sẽ tự động tạo database khi chạy lần đầu nhờ parameter `createDatabaseIfNotExist=true`

Hoặc tạo thủ công:
```sql
CREATE DATABASE customer_management 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### Lỗi 4: "Communications link failure"

**Nguyên nhân:** MySQL không lắng nghe port 3306

**Giải pháp:**
```bash
# Kiểm tra port
sudo netstat -tlnp | grep 3306

# Kiểm tra MySQL config
sudo cat /etc/mysql/mysql.conf.d/mysqld.cnf | grep bind-address
# Nếu cần, sửa bind-address = 127.0.0.1
```

---

## 📝 Migration Data Từ SQLite

Nếu bạn đã có dữ liệu trong SQLite và muốn chuyển sang MySQL:

### Cách 1: Export/Import Thủ Công

```bash
# 1. Export từ SQLite
sqlite3 customer_database.db .dump > sqlite_dump.sql

# 2. Chỉnh sửa file dump (thay đổi syntax)
# - AUTOINCREMENT -> AUTO_INCREMENT
# - INTEGER -> BIGINT
# - TEXT -> VARCHAR/TEXT

# 3. Import vào MySQL
mysql -u root -p customer_management < sqlite_dump.sql
```

### Cách 2: Sử Dụng Tools

- **DB Browser for SQLite** - Export to CSV
- Import CSV vào MySQL qua phpMyAdmin hoặc MySQL Workbench

---

## 📚 Tài Liệu Tham Khảo

- [MySQL Documentation](https://dev.mysql.com/doc/)
- [XAMPP Documentation](https://www.apachefriends.org/docs/)
- [MySQL JDBC Driver](https://dev.mysql.com/doc/connector-j/en/)
- [Hướng dẫn chi tiết: MYSQL_SETUP.md](file:///home/chinghia/Java/customer-management-app/MYSQL_SETUP.md)

---

## ✅ Checklist

Trước khi chạy ứng dụng, đảm bảo:

- [ ] MySQL đã được cài đặt
- [ ] MySQL service đang chạy
- [ ] Có thể kết nối MySQL qua command line
- [ ] Đã compile lại ứng dụng (`mvn clean compile`)
- [ ] Database connection parameters đúng trong `DatabaseManager.java`

---

## 🎯 Kết Luận

Ứng dụng giờ đây sử dụng MySQL, một hệ quản trị cơ sở dữ liệu mạnh mẽ và phổ biến. Điều này giúp:

✅ Tích hợp dễ dàng với các hệ thống khác  
✅ Scalable cho nhiều người dùng  
✅ Công cụ quản lý database tốt hơn  
✅ Phù hợp cho production environment  

**Chúc bạn sử dụng ứng dụng hiệu quả! 🚀**
