# ASM2 - Đại Hiệp Figure Store

## Mô tả dự án

Dự án ASM2 là một ứng dụng web thương mại điện tử bán mô hình figure, được xây dựng bằng **Spring Boot**, **Thymeleaf** và hiện mặc định chạy với **PostgreSQL**.

## Tính năng mới - Thống kê Báo cáo

Dự án đã được bổ sung các tính năng thống kê theo yêu cầu Assignment:

### 1. Thống kê Doanh thu theo Danh mục
- Tổng doanh thu theo từng danh mục sản phẩm
- Tổng số lượng sản phẩm bán ra
- Giá cao nhất, thấp nhất, trung bình

### 2. Thống kê 10 Khách hàng VIP
- Top 10 khách hàng mua nhiều nhất
- Hiển thị: Họ tên, Email, Tổng đơn hàng, Tổng chi tiêu

### 3. Thống kê Sản phẩm Bán chạy
- Top 10 sản phẩm bán chạy nhất
- Hiển thị: Mã SP, Tên, Danh mục, Số lượng bán, Doanh thu

### 4. Thống kê Tổng quan Dashboard
- Tổng doanh thu, đơn hàng, sản phẩm đã bán
- Tỷ lệ tăng trưởng so với kỳ trước
- Số đơn hàng theo trạng thái (Chờ xác nhận, Đang giao, Hoàn thành)

## Cấu trúc code mới thêm

### DTO Classes (Data Transfer Object)
```
src/main/java/com/example/asm/dto/
├── ThongKeDanhMucDTO.java       # Thống kê theo danh mục
├── ThongKeKhachHangVIPDTO.java  # Top khách hàng VIP
├── ThongKeSanPhamBanChayDTO.java # Sản phẩm bán chạy
└── ThongKeTongQuanDTO.java      # Tổng quan dashboard
```

### Service
```
src/main/java/com/example/asm/service/
└── ReportService.java           # Xử lý logic thống kê
```

### Controller
```
src/main/java/com/example/asm/controller/admin/
└── ReportController.java        # Điều hướng trang báo cáo
```

### View (Admin)
```
src/main/resources/templates/admin/
└── reports.html                # Trang thống kê báo cáo
```

## Hướng dẫn chạy dự án

### Yêu cầu
- Java 17+
- Maven 3.6+
- PostgreSQL 15+
- Spring Boot 3.x

### Cấu hình Database
Chỉnh sửa file `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/webmohinh2
spring.datasource.username=postgres
spring.datasource.password=your_password
```

Hoặc dùng biến môi trường theo file mẫu `.env.example`.

### Các bước chạy
```bash
# 1. Clone dự án
git clone https://github.com/your-username/ASM2.git

# 2. Di chuyển vào thư mục dự án
cd ASM2

# 3. Chạy dự án bằng Maven
./mvnw spring-boot:run

# 4. Truy cập trang admin thống kê
http://localhost:8080/admin/reports
```

## Hướng dẫn Upload lên GitHub

### Bước 1: Tạo Repository mới trên GitHub
1. Truy cập https://github.com và đăng nhập
2. Click nút **"+"** → **"New repository"**
3. Đặt tên repository (ví dụ: `ASM2-FigureStore`)
4. Chọn **Public** hoặc **Private**
5. Click **"Create repository"**

### Bước 2: Cấu hình Git trên máy local
```bash
# Di chuyển vào thư mục dự án
cd D:\JAVA5\ASM2_GITHUB

# Cấu hình tên và email (nếu chưa có)
git config user.name "Tên của bạn"
git config user.email "email@example.com"

# Khởi tạo Git repository
git init
```

### Bước 3: Thêm files và Commit
```bash
# Thêm tất cả files vào Git
git add .

# Kiểm tra trạng thái
git status

# Tạo commit
git commit -m "feat: Add statistical reporting features
- Thống kê doanh thu theo danh mục
- Top 10 khách hàng VIP  
- Top 10 sản phẩm bán chạy
- Dashboard tổng quan với biểu đồ"
```

### Bước 4: Kết nối với GitHub và Push
```bash
# Thêm remote repository (thay your-username bằng username GitHub của bạn)
git remote add origin https://github.com/your-username/ASM2-FigureStore.git

# Đổi tên branch thành main
git branch -M main

# Push lên GitHub lần đầu
git push -u origin main
```

### Bước 5: Nhập thông tin đăng nhập
Khi push lần đầu, GitHub sẽ yêu cầu đăng nhập:
- **Username**: Tên đăng nhập GitHub của bạn
- **Password**: Personal Access Token (không phải password thường)

#### Cách tạo Personal Access Token:
1. Truy cập https://github.com/settings/tokens
2. Click **"Generate new token (classic)"**
3. Đặt tên token, chọn expiration
4. Chọn quyền: `repo` (đầy đủ)
5. Copy token và sử dụng làm password khi push

### Các lệnh Git thường dùng sau này
```bash
# Kiểm tra trạng thái
git status

# Thêm thay đổi
git add .

# Commit thay đổi
git commit -m "Mô tả thay đổi"

# Push lên GitHub
git push origin main

# Pull từ GitHub về máy
git pull origin main

# Xem lịch sử commit
git log --oneline
```

## Các trang trong Admin
- **Dashboard**: http://localhost:8080/admin/dashboard
- **Reports (Thống kê)**: http://localhost:8080/admin/reports
- **Products**: http://localhost:8080/admin/products
- **Orders**: http://localhost:8080/admin/orders
- **Customers**: http://localhost:8080/admin/customers

## Tài khoản Demo Admin
- **Username**: admin
- **Password**: admin123

## License
MIT License
