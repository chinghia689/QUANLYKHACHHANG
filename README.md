# 🏢 Ứng Dụng Quản Lý Khách Hàng (Customer Management Application)

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=java)
![SQLite](https://img.shields.io/badge/SQLite-3.45-green?style=for-the-badge&logo=sqlite)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=for-the-badge&logo=apache-maven)

**Ứng dụng Desktop hiện đại để quản lý thông tin khách hàng với giao diện đẹp mắt và tính năng đa dạng**

</div>

---

## ✨ Tính Năng Nổi Bật

### 🎯 Quản Lý Khách Hàng
- ✅ **Thêm mới** khách hàng với đầy đủ thông tin
- ✏️ **Sửa/Cập nhật** thông tin khách hàng
- 🗑️ **Xóa** khách hàng với xác nhận an toàn
- 📊 **Hiển thị danh sách** khách hàng dạng bảng

### 🔍 Tìm Kiếm & Lọc
- 🔎 **Tìm kiếm nhanh** theo tên, số điện thoại, hoặc email
- 🎚️ **Lọc theo loại** khách hàng (VIP, Thường, Tiềm năng)
- 🔄 **Tự động cập nhật** kết quả khi nhập liệu

### 💾 Quản Lý Dữ Liệu
- 📁 **SQLite Database** - Lưu trữ dữ liệu bền vững
- 🔐 **Validation** - Kiểm tra tính hợp lệ của dữ liệu
- 📝 **Tự động tạo ID** cho mỗi khách hàng
- 📅 **Lưu timestamp** khi tạo khách hàng

### 🎨 Giao Diện Hiện Đại
- 🌙 **Dark Theme** với gradient đẹp mắt
- ✨ **Glassmorphism Effects** cho dialogs
- 🎭 **Smooth Animations** và hover effects
- 📱 **Responsive Layout** thích ứng kích thước cửa sổ

---

## 📋 Thông Tin Khách Hàng

Ứng dụng quản lý các thông tin sau:

| Trường | Mô Tả | Bắt Buộc |
|--------|-------|----------|
| 👤 **Họ Tên** | Họ và tên đầy đủ | ✅ Có |
| 📞 **Số Điện Thoại** | Số điện thoại liên hệ (9-11 chữ số) | ❌ Không |
| 📧 **Email** | Địa chỉ email (có validation) | ❌ Không |
| 🏠 **Địa Chỉ** | Địa chỉ nhà/công ty | ❌ Không |
| 🎂 **Ngày Sinh** | Ngày tháng năm sinh | ❌ Không |
| 🏷️ **Loại Khách Hàng** | VIP / Thường / Tiềm năng | ✅ Có |

---

## 🛠️ Công Nghệ Sử Dụng

### Core Technologies
- **Java 17+** - Ngôn ngữ lập trình chính
- **JavaFX 21** - Framework giao diện người dùng
- **SQLite 3.45** - Cơ sở dữ liệu nhúng
- **Maven** - Quản lý dependencies và build

### Architecture
- **MVC Pattern** - Model-View-Controller
- **DAO Pattern** - Data Access Object
- **Service Layer** - Business logic separation
- **Singleton Pattern** - Database connection management

---

## 🚀 Cài Đặt & Chạy Ứng Dụng

### Yêu Cầu Hệ Thống

```bash
Java Development Kit (JDK) 17 hoặc cao hơn
Apache Maven 3.8+
```

### Kiểm Tra Phiên Bản Java

```bash
java -version
```

Nếu chưa có Java 17+, tải tại: https://adoptium.net/

### Build & Run

1️⃣ **Clone hoặc navigate đến thư mục dự án**

```bash
cd customer-management-app
```

2️⃣ **Build dự án với Maven**

```bash
mvn clean compile
```

3️⃣ **Chạy ứng dụng**

```bash
mvn javafx:run
```

### Tạo File JAR Thực Thi

```bash
mvn clean package
java -jar target/customer-management-app-1.0.0.jar
```

---

## 📂 Cấu Trúc Dự Án

```
customer-management-app/
│
├── src/
│   └── main/
│       ├── java/com/customer/
│       │   ├── Main.java                    # Entry point
│       │   ├── model/
│       │   │   ├── Customer.java            # Customer entity
│       │   │   └── CustomerType.java        # Customer type enum
│       │   ├── dao/
│       │   │   ├── DatabaseManager.java     # DB connection
│       │   │   └── CustomerDAO.java         # Data access
│       │   ├── service/
│       │   │   └── CustomerService.java     # Business logic
│       │   ├── controller/
│       │   │   └── MainController.java      # UI controller
│       │   └── ui/
│       │       └── CustomerDialog.java      # Add/Edit dialog
│       │
│       └── resources/
│           ├── styles/
│           │   └── main.css                 # Modern styling
│           ├── views/
│           │   └── main-view.fxml           # Main UI layout
│           └── database/
│               └── schema.sql               # DB schema
│
├── pom.xml                                  # Maven config
├── customer_database.db                     # SQLite database (auto-created)
└── README.md                                # This file
```

---

## 🎮 Hướng Dẫn Sử Dụng

### 1. Thêm Khách Hàng Mới

1. Click nút **"➕ Thêm Mới"**
2. Điền thông tin vào form:
   - Họ tên (bắt buộc)
   - Số điện thoại, Email, Địa chỉ (tùy chọn)
   - Chọn ngày sinh từ DatePicker
   - Chọn loại khách hàng (VIP/Thường/Tiềm năng)
3. Click **"Lưu"** để lưu hoặc **"Hủy"** để đóng

### 2. Sửa Thông Tin Khách Hàng

1. Click chọn khách hàng trong bảng
2. Click nút **"✏️ Sửa"**
3. Chỉnh sửa thông tin trong form
4. Click **"Lưu"** để cập nhật

### 3. Xóa Khách Hàng

1. Click chọn khách hàng trong bảng
2. Click nút **"🗑️ Xóa"**
3. Xác nhận xóa trong dialog

### 4. Tìm Kiếm

- Nhập từ khóa vào ô **"Tìm kiếm"**
- Kết quả tự động lọc theo tên, SĐT, hoặc email

### 5. Lọc Theo Loại

- Chọn loại khách hàng từ dropdown **"Lọc"**
- Chọn "Tất cả" để hiển thị tất cả khách hàng

### 6. Làm Mới Dữ Liệu

- Click nút **"🔄 Làm Mới"** để reset bộ lọc và reload dữ liệu

---

## 🎨 Giao Diện

Ứng dụng sử dụng thiết kế hiện đại với:

- **Dark Theme**: Nền gradient từ deep purple đến dark blue
- **Vibrant Colors**: Purple (#8B5CF6), Blue (#3B82F6), Pink (#EC4899)
- **Glassmorphism**: Hiệu ứng kính mờ cho dialogs
- **Smooth Transitions**: Animation mượt mà cho mọi tương tác
- **Professional Typography**: Font hiện đại dễ đọc

### Color Palette

```css
Primary Purple:   #8B5CF6
Secondary Blue:   #3B82F6
Accent Pink:      #EC4899
Success Green:    #10B981
Danger Red:       #EF4444
Background Dark:  #1e1b4b → #0f172a (gradient)
```

---

## ⚙️ Kiến Trúc & Design Patterns

### 1. **MVC (Model-View-Controller)**
- **Model**: `Customer`, `CustomerType`
- **View**: FXML files, CSS styling
- **Controller**: `MainController`, `CustomerDialog`

### 2. **DAO Pattern**
- `CustomerDAO` - Tách biệt logic truy cập database
- `DatabaseManager` - Quản lý connection

### 3. **Service Layer**
- `CustomerService` - Business logic và validation
- Tách biệt khỏi UI và database layer

### 4. **Singleton Pattern**
- `DatabaseManager` - Đảm bảo chỉ có 1 connection instance

---

## 🔒 Validation Rules

Ứng dụng có các quy tắc validation sau:

✅ **Họ tên**: 
- Bắt buộc nhập
- Tối thiểu 2 ký tự

✅ **Email**:
- Pattern: `username@domain.extension`
- Ví dụ: `customer@example.com`

✅ **Số điện thoại**:
- 9-11 chữ số
- Cho phép dấu cách và dấu gạch ngang

✅ **Loại khách hàng**:
- Bắt buộc chọn 1 trong 3 loại

---

## 🐛 Xử Lý Lỗi

Ứng dụng có các dialog thông báo:

- ✅ **Success**: Thông báo khi thao tác thành công
- ❌ **Error**: Hiển thị lỗi khi có exception
- ⚠️ **Warning**: Cảnh báo khi chưa chọn item
- ℹ️ **Info**: Thông tin bổ sung cho người dùng

---

## 📊 Database Schema

```sql
CREATE TABLE customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    date_of_birth TEXT,
    customer_type TEXT NOT NULL,
    created_date TEXT NOT NULL
);
```

**Indexes** để tối ưu tìm kiếm:
- `idx_full_name`
- `idx_phone`
- `idx_email`
- `idx_customer_type`

---

## 🚧 Tính Năng Tương Lai (Roadmap)

- [ ] Export dữ liệu ra Excel/CSV
- [ ] Import khách hàng từ file
- [ ] Lịch sử giao dịch cho mỗi khách hàng
- [ ] Gửi email/SMS hàng loạt
- [ ] Thống kê và biểu đồ
- [ ] Backup & Restore database
- [ ] Multi-language support
- [ ] Dark/Light theme toggle
- [ ] Print customer list

---

## 🤝 Đóng Góp

Mọi đóng góp đều được hoan nghênh! Nếu bạn muốn cải thiện ứng dụng:

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

---

## 📝 License

Dự án này được phát hành dưới giấy phép MIT License.

---

## 👨‍💻 Tác Giả

Được xây dựng với ❤️ bởi **JavaFX Developer**

---

## 📞 Liên Hệ & Hỗ Trợ

Nếu bạn gặp vấn đề hoặc có thắc mắc:

- 🐛 **Báo lỗi**: Tạo issue trên GitHub
- 💡 **Đề xuất tính năng**: Tạo feature request
- 📧 **Email**: support@example.com

---

<div align="center">

**⭐ Nếu bạn thấy dự án hữu ích, hãy cho một star! ⭐**

Made with JavaFX 21 | Powered by SQLite

</div>
