# HUONG DAN SU DUNG WEBSITE ASM2

## 1. Tong quan he thong
- Nen tang: Spring Boot + Thymeleaf + SQL Server.
- Muc tieu: Website ban mo hinh figure, co 2 nhom chuc nang:
1. User: xem san pham, gio hang, dat hang, theo doi don, danh gia, yeu thich.
2. Admin: quan ly du lieu, don hang, nhap kho, thong ke bao cao.

## 2. Chuan bi moi truong
### 2.1 Yeu cau
- Java 17+
- Maven 3.6+
- SQL Server

### 2.2 Cau hinh
Chinh file `src/main/resources/application.properties`:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- Cau hinh mail (neu can gui mail)
- Cau hinh QR thanh toan:
  - `payment.qr.bank-id`
  - `payment.qr.account`
  - `payment.qr.acq-name`
  - `payment.qr.template`

### 2.3 Chay du an
```powershell
.\mvnw spring-boot:run
```
Truy cap mac dinh: `http://localhost:8080`

## 3. Cac luong chuc nang user

### 3.1 Dang ky, dang nhap, dang xuat
- Dang ky: `GET/POST /account/sign-up`
  - Validate:
    - Mat khau va xac nhan mat khau phai trung.
    - Username khong trung.
    - Email khong trung.
  - Dang ky thanh cong chuyen den trang login.
- Dang nhap: `GET/POST /auth/login`
  - Sai thong tin: hien message loi.
  - Dung thong tin: luu `user` vao session.
- Dang xuat: `GET /auth/logoff`
  - Xoa `user` khoi session.

### 3.2 Duyet san pham
- Trang chu: `GET /` hoac `/home/index`
  - Hien thi danh muc va danh sach san pham noi bat.
- Danh sach san pham: `GET /product/list`
  - Ho tro loc theo:
    - Danh muc (`cid`)
    - Tu khoa (`keyword`)
    - Khoang gia (`price`: `300to1000`, `1000to2000`, `above2000`)
    - Sap xep (`sort`: `newest`, `priceAsc`, `priceDesc`, `nameAsc`)
- Chi tiet san pham: `GET /product/detail/{id}`
  - Hien thi:
    - Thong tin san pham
    - San pham lien quan cung danh muc
    - Danh gia va diem trung binh

### 3.3 Gio hang
- Xem gio: `GET /cart/view`
- Them vao gio: `GET /cart/add/{maBienThe}`
  - Kiem tra dang nhap.
  - Kiem tra ton kho truoc khi them.
- Sua so luong: `GET /cart/update/{maGioHang}?qty=...`
- Xoa 1 dong: `GET /cart/remove/{maGioHang}`
- Xoa toan bo: `GET /cart/clear`

### 3.4 Dat hang va thanh toan
- Checkout: `GET /order/checkout`
  - Neu chua dang nhap -> chuyen login.
  - Neu gio hang rong -> ve home.
  - Tinh:
    - `originalAmount` (tong tien hang)
    - `discountAmount` (voucher)
    - `amount` (tong cuoi)
- Ap dung voucher: `POST /order/apply-voucher`
  - Validate:
    - Ma ton tai va dang hoat dong
    - Con so luot
    - Trong thoi gian ap dung
    - Dat gia tri don toi thieu
  - Voucher hop le duoc luu trong session.
- Huy voucher: `GET /order/remove-voucher`
- Mua hang: `POST /order/purchase`
  - Validate dia chi khong chua ky tu dac biet `+ - * / ...`
  - Tao don hang va chi tiet don.
  - Phi van chuyen mac dinh: `30000`.
  - Tru luot voucher neu su dung hop le.
  - Payment:
    - `COD` -> `/order/success`
    - `CHUYEN_KHOAN` -> `/order/payment-qr/{id}`
- Thanh toan QR:
  - `GET /order/payment-qr/{id}` tao URL QR theo cau hinh.
  - Khach chuyen khoan theo ma QR va noi dung chuyen khoan.
  - Admin tu kiem tra giao dich, sau do cap nhat thanh toan trong trang quan tri don hang.

### 3.5 Don hang, yeu thich, danh gia, ho so
- Lich su don: `GET /order/list`
- Chi tiet don: `GET /order/detail/{id}` (chi xem don cua chinh minh)
- Yeu thich:
  - Xem: `GET /favorite/view`
  - Bat/tat: `GET /favorite/toggle/{maMoHinh}`
  - Xoa: `GET /favorite/remove/{maMoHinh}`
- Danh gia san pham: `POST /rating/add`
- Ho so:
  - Xem: `GET /account/profile`
  - Cap nhat thong tin: `POST /account/update-profile`
  - Doi mat khau: `POST /account/change-password`

## 4. Cac luong chuc nang admin

### 4.1 Dashboard va bao cao
- Dashboard: `GET /admin/dashboard`
  - Tong hop 30 ngay gan nhat:
    - Tong doanh thu, tong don, tong san pham ban
    - Top khach hang, top san pham
    - Bieu do doanh thu theo thang
    - Don hang theo trang thai
- Bao cao chi tiet: `GET /admin/reports`
  - Cho phep loc theo `startDate`, `endDate`.
  - Mac dinh 30 ngay gan nhat neu khong truyen tham so.
  - Hien thi:
    - Doanh thu theo danh muc
    - Top 10 khach VIP
    - Top 10 san pham ban chay
    - Bieu do doanh thu theo thang
    - Bieu do don theo trang thai

### 4.2 CRUD du lieu
- Quan ly san pham: `/admin/products`
- Quan ly bien the: `/admin/variants/{maMoHinh}`
- Quan ly danh muc: `/admin/categories`
- Quan ly hang san xuat: `/admin/manufacturers`
- Quan ly khuyen mai: `/admin/promotions`
- Quan ly tai khoan: `/admin/accounts`
  - Co toggle khoa/mo khoa tai khoan.

### 4.3 Quan ly don hang admin
- Danh sach + loc + phan trang: `GET /admin/orders`
- Xem chi tiet qua API: `GET /admin/orders/api/{id}`
- Cap nhat trang thai: `POST /admin/orders/update`
- Huy don: `POST /admin/orders/cancel/{id}`
- Xoa don: `GET /admin/orders/delete/{id}`
- Thong ke nhanh trang thai: `GET /admin/orders/stats`

### 4.4 Luong nhap kho
- Man hinh nhap kho: `GET /admin/inbound`
- Tao don nhap: `POST /admin/inbound/create`
- Sua ghi chu don nhap: `POST /admin/inbound/{id}/update`
- Xoa don nhap: `POST /admin/inbound/{id}/delete`
- Them chi tiet nhap: `POST /admin/inbound/{id}/detail`
- Sua chi tiet nhap: `POST /admin/inbound/{donId}/detail/{detailId}/update`
- Xoa chi tiet nhap: `POST /admin/inbound/{donId}/detail/{detailId}/delete`

Logic kho:
- Them chi tiet nhap: cong ton kho bien the.
- Sua chi tiet nhap: dieu chinh ton theo chenhlech so luong.
- Xoa chi tiet/don nhap: tru ton kho tuong ung.
- Don nhap luu `tongTienNhap` = tong(`giaNhap * soLuongNhap`) cua chi tiet.

## 5. Logic nghiep vu quan trong
- Xac thuc hien tai dung session (`AuthService`), luu user trong key `user`.
- Voucher duoc validate 2 lan:
1. Khi apply voucher (controller).
2. Khi place order (service) de tranh voucher het han/het luot do race condition.
- Ton kho:
1. Kiem tra ton kho khi them vao gio.
2. Tru ton kho khi user xac nhan thanh toan QR.
- Bao cao dung SQL native query trong `ReportService` de tong hop KPI.

## 6. Test hien co
Test code nam tai:
- `src/test/java/com/example/asm/AsmApplicationTests.java`
- `src/test/java/com/example/asm/ServletInitializerTest.java`
- `src/test/java/com/example/asm/controller/HomeControllerTest.java`

Noi dung test:
- `AsmApplicationTests`:
  - Kiem tra `AsmApplication` co annotation `@SpringBootApplication`.
- `ServletInitializerTest`:
  - Kiem tra `ServletInitializer.configure(...)` dang ky dung source app.
- `HomeControllerTest`:
  - Kiem tra action `index()` tra ve view `fragments/home`.
  - Kiem tra model co `topItems`.
  - Kiem tra `getCategories()` lay du lieu dung tu `CategoryService`.

## 7. Cach chay test
```powershell
.\mvnw -q test
```
Trang thai hien tai trong workspace: `PASS` (3 class test tren chay thanh cong).

## 8. Luu y khi mo rong test
- Nen bo sung test cho:
1. Luong dat hang + voucher + payment QR.
2. CartService va logic ton kho.
3. Luong admin (CRUD va inbound).
4. ReportService (test SQL aggregation voi du lieu mau).
- Uu tien tach business logic sang service de test unit de hon.
