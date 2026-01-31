-- Insert 10 sample customers for testing
-- customer_type must match enum: VIP, REGULAR, POTENTIAL
INSERT INTO customers (full_name, phone, email, address, date_of_birth, customer_type, created_date) VALUES
('Nguyễn Văn An', '0901234567', 'nguyenvanan@gmail.com', '123 Lê Lợi, Quận 1, TP.HCM', '1985-03-15', 'VIP', '2024-01-15 10:30:00'),
('Trần Thị Bảo', '0912345678', 'tranbao@yahoo.com', '456 Nguyễn Huệ, Quận 1, TP.HCM', '1990-07-22', 'REGULAR', '2024-02-20 14:45:00'),
('Lê Hoàng Cường', '0923456789', 'lehoangcuong@gmail.com', '789 Hai Bà Trưng, Quận 3, TP.HCM', '1988-11-08', 'VIP', '2024-03-10 09:15:00'),
('Phạm Thị Diễm', '0934567890', 'phamdiem@hotmail.com', '321 Võ Văn Tần, Quận 3, TP.HCM', '1992-05-30', 'POTENTIAL', '2024-04-05 16:20:00'),
('Hoàng Văn Em', '0945678901', 'hoangem@gmail.com', '654 Cách Mạng Tháng 8, Quận 10, TP.HCM', '1987-09-12', 'REGULAR', '2024-05-12 11:00:00'),
('Đặng Thị Phượng', '0956789012', 'dangphuong@gmail.com', '987 Lý Thường Kiệt, Quận 10, TP.HCM', '1995-02-18', 'VIP', '2024-06-18 13:30:00'),
('Vũ Minh Giang', '0967890123', 'vugiang@yahoo.com', '147 Điện Biên Phủ, Bình Thạnh, TP.HCM', '1991-12-25', 'POTENTIAL', '2024-07-22 15:45:00'),
('Bùi Thị Hà', '0978901234', 'buiha@gmail.com', '258 Phan Đăng Lưu, Phú Nhuận, TP.HCM', '1989-08-07', 'REGULAR', '2024-08-08 10:10:00'),
('Đỗ Văn Hùng', '0989012345', 'dohung@hotmail.com', '369 Hoàng Văn Thụ, Tân Bình, TP.HCM', '1993-04-14', 'VIP', '2024-09-14 12:25:00'),
('Mai Thị Lan', '0990123456', 'mailan@gmail.com', '741 Trường Chinh, Tân Bình, TP.HCM', '1994-10-20', 'POTENTIAL', '2024-10-20 08:50:00');
