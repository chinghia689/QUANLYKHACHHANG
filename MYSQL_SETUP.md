# Hướng Dẫn Cài Đặt và Cấu Hình MySQL/XAMPP

## 🎯 Tổng Quan

Ứng dụng đã được cấu hình để sử dụng MySQL database thay vì SQLite. Bạn cần cài đặt và khởi động MySQL server (thông qua XAMPP hoặc MySQL standalone).

---

## 📦 Cách 1: Cài Đặt XAMPP (Khuyến nghị cho người mới)

### 1. Tải XAMPP

```bash
# Download XAMPP for Linux
wget https://sourceforge.net/projects/xampp/files/XAMPP%20Linux/8.2.12/xampp-linux-x64-8.2.12-0-installer.run

# Hoặc truy cập: https://www.apachefriends.org/download.html
```

### 2. Cài Đặt XAMPP

```bash
# Cấp quyền thực thi
chmod +x xampp-linux-x64-8.2.12-0-installer.run

# Chạy installer với sudo
sudo ./xampp-linux-x64-8.2.12-0-installer.run
```

Làm theo hướng dẫn trên màn hình, chọn các component:
- ✅ MySQL
- ✅ phpMyAdmin (tùy chọn, để quản lý database qua web)
- ❌ Apache (không cần thiết cho ứng dụng này)

### 3. Khởi Động MySQL

```bash
# Khởi động XAMPP MySQL
sudo /opt/lampp/lampp startmysql

# Hoặc khởi động toàn bộ XAMPP
sudo /opt/lampp/lampp start
```

### 4. Kiểm Tra MySQL Đang Chạy

```bash
# Kiểm tra MySQL service
sudo /opt/lampp/lampp status

# Kết nối MySQL CLI (test)
/opt/lampp/bin/mysql -u root -p
# (Nhấn Enter khi hỏi password - mặc định là rỗng)
```

---

## 📦 Cách 2: Cài Đặt MySQL Standalone

Nếu không muốn dùng XAMPP, cài MySQL trực tiếp:

```bash
# Cài đặt MySQL Server
sudo apt update
sudo apt install mysql-server -y

# Khởi động MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# Kiểm tra trạng thái
sudo systemctl status mysql
```

### Cấu Hình MySQL (nếu cần)

```bash
# Đăng nhập MySQL
sudo mysql -u root

# Tạo user mới (tùy chọn)
CREATE USER 'root'@'localhost' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

## ⚙️ Cấu Hình Ứng Dụng

### Thông Tin Kết Nối Mặc Định

File: `DatabaseManager.java`

```java
private static final String DB_HOST = "localhost";
private static final String DB_PORT = "3306";
private static final String DB_NAME = "customer_management";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = ""; // XAMPP mặc định: rỗng
```

### Thay Đổi Cấu Hình (nếu cần)

Nếu bạn có password khác hoặc dùng user khác, sửa trong `DatabaseManager.java`:

```java
private static final String DB_PASSWORD = "your_password_here";
```

---

## 🚀 Chạy Ứng Dụng

### 1. Đảm Bảo MySQL Đang Chạy

```bash
# Với XAMPP
sudo /opt/lampp/lampp status

# Với MySQL standalone
sudo systemctl status mysql
```

### 2. Compile Lại Ứng Dụng

```bash
cd /home/chinghia/Java/customer-management-app
mvn clean compile
```

### 3. Chạy Ứng Dụng

```bash
mvn javafx:run
```

---

## 🗄️ Quản Lý Database

### Sử Dụng phpMyAdmin (XAMPP)

1. Truy cập: http://localhost/phpmyadmin
2. Login: username `root`, password để trống
3. Database `customer_management` sẽ được tạo tự động khi chạy app

### Sử Dụng MySQL Command Line

```bash
# Kết nối MySQL
mysql -u root -p
# (Nhấn Enter nếu không có password)

# Chọn database
USE customer_management;

# Xem tables
SHOW TABLES;

# Xem dữ liệu
SELECT * FROM customers;

# Thoát
EXIT;
```

---

## 🔧 Xử Lý Sự Cố

### Lỗi 1: "Can't connect to MySQL server"

**Nguyên nhân:** MySQL chưa chạy

**Giải pháp:**
```bash
# Với XAMPP
sudo /opt/lampp/lampp startmysql

# Với MySQL standalone
sudo systemctl start mysql
```

### Lỗi 2: "Access denied for user 'root'"

**Nguyên nhân:** Sai password

**Giải pháp:**
- Kiểm tra password trong `DatabaseManager.java`
- Reset MySQL root password nếu cần

### Lỗi 3: "Communications link failure"

**Nguyên nhân:** Port 3306 bị chặn hoặc MySQL không lắng nghe

**Giải pháp:**
```bash
# Kiểm tra port 3306
sudo netstat -tlnp | grep 3306

# Nếu không có output, MySQL chưa chạy
```

### Lỗi 4: "Driver not found"

**Nguyên nhân:** MySQL connector chưa được download

**Giải pháp:**
```bash
# Maven sẽ tự động download, nhưng có thể force update
mvn clean install -U
```

---

## 📊 So Sánh SQLite vs MySQL

| Tính Năng | SQLite (Cũ) | MySQL (Mới) |
|-----------|-------------|-------------|
| **Setup** | Không cần | Cần cài MySQL |
| **Performance** | Tốt cho ít data | Tốt cho nhiều data |
| **Concurrent Access** | Limited | Excellent |
| **Data Size** | < 2GB | Unlimited |
| **Backup** | Copy file | mysqldump |
| **Multi-user** | Không | Có |

---

## 🔄 Quay Lại SQLite (nếu cần)

Nếu không muốn dùng MySQL, bạn có thể quay lại SQLite:

### 1. Sửa `pom.xml`

```xml
<!-- Thay MySQL bằng SQLite -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.0.0</version>
</dependency>
```

### 2. Sửa `DatabaseManager.java`

```java
private static final String DB_URL = "jdbc:sqlite:customer_database.db";
```

---

## 📝 Ghi Chú

✅ **Database tự động tạo:** Ứng dụng sẽ tự động tạo database `customer_management` khi chạy lần đầu

✅ **Schema tự động:** Bảng `customers` sẽ được tạo tự động từ file `schema.sql`

✅ **UTF-8 Support:** Hỗ trợ tiếng Việt đầy đủ với `utf8mb4`

⚠️ **Security:** Trong production, đổi password mặc định của MySQL!

---

## 🆘 Hỗ Trợ

Nếu gặp vấn đề:

1. Kiểm tra MySQL đang chạy: `sudo /opt/lampp/lampp status`
2. Kiểm tra logs của ứng dụng trong console
3. Kiểm tra MySQL error log: `/opt/lampp/logs/mysql_error.log`
