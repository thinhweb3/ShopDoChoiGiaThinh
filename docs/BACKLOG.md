# PRODUCT BACKLOG - ASM2 Figure Store

## 1. Scope
- Product: Website ban mo hinh figure (Spring Boot + Thymeleaf + SQL Server)
- Actors: Guest, User, Admin
- Core modules: Auth, Product Catalog, Cart, Checkout, Order, Favorite, Rating, Profile, Admin CRUD, Inbound, Dashboard & Reports

## 2. Priority Scale
- P0: Bat buoc de he thong van hanh
- P1: Quan trong, anh huong trai nghiem chinh
- P2: Nang cap huu ich, co the trien khai sau
- P3: Toi uu/hoan thien

## 3. Backlog by Epic

### EPIC A - Authentication & Account

#### A-01 Dang ky tai khoan
- Priority: P0
- User Story: La khach, toi muon dang ky tai khoan de mua hang va quan ly don.
- Acceptance Criteria:
  - Validate username/email khong trung
  - Validate mat khau va xac nhan mat khau trung nhau
  - Dang ky thanh cong dieu huong den trang dang nhap
- Micro-functions derived from "I want":
  - Display all registration fields (username, email, password, confirm, optional phone)
  - Perform async uniqueness check for username/email before submission
  - Enforce password policy inline, hash the password, persist the new account, handle failures
  - Chain post-register redirect to login (and show toast)
- Clarifications / redundancy check: Date-based email confirmation is not currently defined, so do not duplicate with password reset flow; keep all validation messaging within this flow rather than splitting into a separate validation page.

#### A-02 Dang nhap/dang xuat
- Priority: P0
- User Story: La user, toi muon dang nhap de su dung cac chuc nang ca nhan.
- Acceptance Criteria:
  - Dang nhap sai hien thi thong bao loi
  - Dang nhap dung luu user vao session
  - Dang xuat xoa session va quay ve trang chu
- Micro-functions derived from "I want":
  - Show login form, offer remember-me option, and surface validation errors
  - Authenticate credentials, establish session data for user+roles, and refresh user context
  - Invalidate session on logout, clear cookies, redirect home with feedback
- Clarifications / redundancy check: Login/logout is the single canonical path; avoid duplicating session cleanup in other flows (e.g., voucher checkout) to keep responsibility centralized here.

#### A-03 Quen mat khau qua email
- Priority: P1
- User Story: La user, toi muon reset mat khau neu quen.
- Acceptance Criteria:
  - Nhap email hop le moi duoc gui reset
  - Link reset co han
  - Mat khau moi duoc hash va cap nhat thanh cong
- Micro-functions derived from "I want":
  - Provide an email input, validate format, and disable submit when invalid
  - Generate expiring token, send reset link, log event for audit
  - Validate token on landing page, allow password change, hash before persisting
- Clarifications / redundancy check: This flow already covers the admin unlock/pass changes; avoid creating a separate "forgot password" option inside profile management (A-04) to reduce duplication.

#### A-04 Quan ly profile
- Priority: P1
- User Story: La user, toi muon cap nhat thong tin ca nhan va doi mat khau.
- Acceptance Criteria:
  - Cap nhat profile thanh cong va persist DB
  - Doi mat khau yeu cau mat khau cu dung
  - Mat khau moi dat chinh sach toi thieu
- Micro-functions derived from "I want":
  - Fetch current profile, show editable form, validate contact fields
  - Let user toggle between updating profile data and changing password
  - For password change require old password, apply policy, update hash
- Clarifications / redundancy check: Since A-03 handles forgotten-password, keep this strictly for authenticated password updates to avoid overlapping functionality.

### EPIC B - Product Discovery

#### B-01 Trang chu va san pham noi bat
- Priority: P0
- User Story: La khach, toi muon xem nhanh san pham noi bat tren trang chu.
- Acceptance Criteria:
  - Hien thi top san pham theo tieu chi he thong
  - Hien thi danh muc de dieu huong
- Micro-functions derived from "I want":
  - Query featured products (e.g., best-selling, newest) with pagination limits
  - Render hero area, product grid, and quick links to categories
  - Offer inline navigation to catalog or promotions
- Clarifications / redundancy check: The hero list overlaps with B-02/Paginated listing; avoid building multiple carousel components for the same data by reusing catalog filters.

#### B-02 Danh sach san pham + filter/sort
- Priority: P0
- User Story: La khach, toi muon loc va sap xep de tim san pham phu hop.
- Acceptance Criteria:
  - Filter theo danh muc, tu khoa, khoang gia
  - Sort theo moi nhat, gia tang/giam, ten
  - Ket qua filter/sort dung voi query params
- Micro-functions derived from "I want":
  - Build filter panel (category tree, keyword search, price slider)
  - Apply sort options via API (newest, price asc/desc, name)
  - Keep filter state in query params and show current tags
- Clarifications / redundancy check: B-02 and B-01 should share the same backend listing API to prevent duplicate implementation; avoid creating a separate page that does exactly what the homepage grid already covers.

#### B-03 Chi tiet san pham
- Priority: P0
- User Story: La khach, toi muon xem chi tiet san pham truoc khi mua.
- Acceptance Criteria:
  - Hien thi thong tin co ban + gia
  - Hien thi bien the, ton kho theo bien the
  - Hien thi danh gia va diem trung binh
  - Goi y san pham lien quan
- Micro-functions derived from "I want":
  - Display full product metadata (description, specs, pricing, promotion badges)
  - Show variant picker, update stock/price live, and surface availability per variant
  - Present aggregated reviews, average rating, and related products carousel
- Clarifications / redundancy check: The related products panel may duplicate content from "Trang chu" (B-01); if so, reuse the same recommendation engine instead of re-implementing.

### EPIC C - Cart & Checkout

#### C-01 Quan ly gio hang
- Priority: P0
- User Story: La user, toi muon them/sua/xoa san pham trong gio.
- Acceptance Criteria:
  - Them gio hang yeu cau dang nhap
  - Kiem tra ton kho truoc khi them
  - Cap nhat so luong khong vuot ton kho
  - Co the xoa tung dong va clear toan bo
- Micro-functions derived from "I want":
  - Require login, validate stock for variant before add, notify user on failure
  - Support quantity adjustments with live subtotal updates
  - Offer delete per line and clear-all option
- Clarifications / redundancy check: Avoid duplicating cart validation logic inside checkout or voucher flows by centralizing stock checks in this module.

#### C-02 Checkout co voucher
- Priority: P0
- User Story: La user, toi muon ap ma giam gia khi thanh toan.
- Acceptance Criteria:
  - Voucher duoc kiem tra trang thai/thoi gian/so luot/gia tri toi thieu
  - Luu voucher hop le vao session checkout
  - Tinh dung originalAmount, discountAmount, finalAmount
- Micro-functions derived from "I want":
  - Present voucher entry field and show validation state
  - Verify voucher constraints (validity, activation, usage counts) via API before applying
  - Calculate basis amounts and display new totals instantly
- Clarifications / redundancy check: Voucher logic overlaps with promotions management in E-03; ensure promotions CRUD and voucher redemption share validation services to stay DRY.

#### C-03 Dat hang COD
- Priority: P0
- User Story: La user, toi muon dat hang COD nhanh.
- Acceptance Criteria:
  - Validate thong tin dia chi
  - Tao don hang + chi tiet don
  - Phi ship mac dinh 30,000 VND
  - Dieu huong trang thanh cong
- Micro-functions derived from "I want":
  - Collect/shown default shipping address, phone, contact info
  - Confirm order summary, save order + order lines with COD payment flag
  - Apply fixed shipping fee and final amount before redirecting to success
- Clarifications / redundancy check: Since C-04 handles QR, keep COD-specific logic isolated (e.g., no payment gateway call) to avoid branching inside a single checkout path.

#### C-04 Dat hang chuyen khoan QR
- Priority: P1
- User Story: La user, toi muon thanh toan qua QR.
- Acceptance Criteria:
  - Tao URL QR tu cau hinh he thong
  - Xac nhan thanh toan chi cho don cua chinh user
  - Sau xac nhan: cap nhat trang thai thanh toan va tru ton kho
- Micro-functions derived from "I want":
  - Generate QR payload (amount, order id), show QR image, provide copy/paste data
  - Poll/receive webhook confirming payment tied to current order and user
  - Update payment status, reduce stock, notify user
- Clarifications / redundancy check: The confirmation step may currently duplicate order-status updates from E-06; reuse the admin order update routine to avoid inconsistencies.

### EPIC D - Orders, Favorites, Ratings

#### D-01 Lich su don va chi tiet don
- Priority: P1
- User Story: La user, toi muon xem lich su mua hang.
- Acceptance Criteria:
  - Hien thi danh sach don theo user dang nhap
  - Chi tiet don chi cho phep xem don so huu boi user
- Micro-functions derived from "I want":
  - Aggregate orders by user, show status badges, allow paging
  - Provide detailed view for each order with item breakdown, totals, shipping info
- Clarifications / redundancy check: Avoid creating a second API for order details when the admin order endpoint (E-06) already returns similar data; share DTOs.

#### D-02 Yeu thich san pham
- Priority: P2
- User Story: La user, toi muon danh dau san pham yeu thich.
- Acceptance Criteria:
  - Toggle yeu thich o trang chi tiet/list
  - Xem danh sach yeu thich rieng
  - Xoa khoi danh sach yeu thich
- Micro-functions derived from "I want":
  - Allow authenticated users to toggle favorites via button, with animation and state sync
  - Provide a dedicated favorites list accessible from profile/navigation
  - Support removing individual entries or bulk-clear favorites
- Clarifications / redundancy check: Ensure the favorites list reuses product card UI (same as B-02) to avoid redundant markup.

#### D-03 Danh gia san pham
- Priority: P2
- User Story: La user, toi muon danh gia sau khi mua.
- Acceptance Criteria:
  - User dang nhap moi duoc danh gia
  - Diem danh gia nam trong khoang hop le
  - Diem trung binh duoc cap nhat dung
- Micro-functions derived from "I want":
  - Provide review form on product page with rating picker, required text, and purchase verification
  - Validate rating bounds, save review, recalc average rating atomically
  - Surface reviews in list with pagination/sorting
- Clarifications / redundancy check: Avoid adding rating creation capabilities into admin order flows unless necessary; keep review moderation separate (if planned) to stay focused.

### EPIC E - Admin Operations

#### E-01 Admin Dashboard
- Priority: P1
- User Story: La admin, toi muon xem KPI tong quan he thong.
- Acceptance Criteria:
  - Tong hop doanh thu/don/san pham theo 30 ngay
  - Bieu do doanh thu theo thang
  - Thong ke don hang theo trang thai
- Micro-functions derived from "I want":
  - Pull last 30-day metrics (orders, revenue, new customers) and compute deltas
  - Render charts for monthly revenue trends and status breakdowns
  - Provide quick links to orders/reports from dashboards
- Clarifications / redundancy check: Ensure dashboard data is sourced from same queries as E-02 reports to avoid conflicting numbers.

#### E-02 Admin Reports
- Priority: P1
- User Story: La admin, toi muon xem bao cao chi tiet co loc thoi gian.
- Acceptance Criteria:
  - Loc duoc theo startDate/endDate
  - Hien thi doanh thu theo danh muc
  - Hien thi top 10 khach VIP
  - Hien thi top 10 san pham ban chay
- Micro-functions derived from "I want":
  - Build date-range picker and apply to queries for revenue, categories, VIPs, top products
  - Show tabular data with download/export action (if needed)
  - Highlight top 10 lists with badges and allow quick navigation to customer/product
- Clarifications / redundancy check: Report filtering overlaps with dashboard summaries; reuse filter module to avoid fragmented UX.

#### E-03 CRUD Product/Catalog/Manufacturer/Promotion
- Priority: P0
- User Story: La admin, toi muon quan ly danh muc du lieu ban hang.
- Acceptance Criteria:
  - Day du Create/Read/Update/Delete
  - Validate du lieu truoc khi luu
  - Upload hinh san pham hoat dong
- Micro-functions derived from "I want":
  - Provide master data UI for products, categories, manufacturers, promotions with pagination/search
  - Enforce field validation (names, SKUs, dates) before saving
  - Handle file uploads for product images and store references
- Clarifications / redundancy check: This epic shares validation needs with E-07 (inbound); centralize field validators/services to keep rules consistent.

#### E-04 Quan ly bien the
- Priority: P1
- User Story: La admin, toi muon quan ly bien the cua tung mo hinh.
- Acceptance Criteria:
  - Tao/sua/xoa bien the theo ma mo hinh
  - Kiem soat ton kho theo bien the
- Micro-functions derived from "I want":
  - Allow admin to add/edit/delete variant records linked to products
  - Manage stock per variant, including warnings when quantity falls below thresholds
- Clarifications / redundancy check: Since E-03 product CRUD may already let variants, ensure this module extends that data rather than duplicating variant forms.

#### E-05 Quan ly tai khoan
- Priority: P1
- User Story: La admin, toi muon khoa/mo khoa tai khoan nguoi dung.
- Acceptance Criteria:
  - Toggle active/inactive
  - Tai khoan bi khoa khong dang nhap duoc
- Micro-functions derived from "I want":
  - Display user list with status indicator and toggle control
  - Prevent locked accounts from authenticating and surface reason
- Clarifications / redundancy check: Reuse admin user listing from E-03 where possible; do not duplicate user-management APIs.

#### E-06 Quan ly don hang admin
- Priority: P0
- User Story: La admin, toi muon xu ly vong doi don hang.
- Acceptance Criteria:
  - Loc + phan trang danh sach don
  - Xem chi tiet don qua API va UI
  - Cap nhat trang thai don dung workflow
  - Huy/xoa don theo quyen
- Micro-functions derived from "I want":
  - Provide filters (status, date, customer) and pagination for all orders
  - Show detail view with timeline, history, line items
  - Update status with workflow rules and allow cancellations per permissions
- Clarifications / redundancy check: Avoid separate order detail API for admin vs. customer; share service with role-based responses to simplify maintenance.

#### E-07 Nhap kho (Inbound)
- Priority: P1
- User Story: La admin, toi muon tao phieu nhap kho va dong bo ton kho.
- Acceptance Criteria:
  - Tao/sua/xoa don nhap
  - Them/sua/xoa chi tiet nhap
  - Ton kho cap nhat theo chenhlech so luong
  - TongTienNhap tinh dung theo chi tiet
- Micro-functions derived from "I want":
  - Let admin create inbound orders, attach vendor info, select products/variants, specify quantities/costs
  - Update inventory balances after commit, calculate total cost per inbound document
  - Support editing/deleting inbound lines with stock rollback on remove
- Clarifications / redundancy check: Inbound stock adjustments should reuse the inventory service backing C-01/C-04 to guarantee consistency.

### EPIC F - Quality, Security, DevOps

#### F-01 Test coverage cho luong critical
- Priority: P0
- User Story: La team dev, toi muon giam loi hoi quy qua test tu dong.
- Acceptance Criteria:
  - Bo sung test cho checkout + voucher + QR payment
  - Bo sung test cart + stock rules
  - Bo sung test admin order/inbound/report
- Micro-functions derived from "I want":
  - Build unit/integration tests for cart/checkout steps, ensuring stock/discount logic stays intact
  - Add regression suite for order flow, voucher redemption, QR payment confirmation, admin inbound and reports
- Clarifications / redundancy check: Separate test cases for each workflow to avoid large end-to-end tests that duplicate effort.

#### F-02 Phan quyen route theo role
- Priority: P0
- User Story: La chu he thong, toi muon route admin chi cho admin.
- Acceptance Criteria:
  - Route /admin/** chan voi user thuong
  - API nhay cam tra 403 khi khong du quyen
- Micro-functions derived from "I want":
  - Apply security filter/interceptor to guard /admin/** paths
  - Add API-level authorization checks per controller
- Clarifications / redundancy check: Keep RBAC centralized in security configuration rather than sprinkling checks inside each controller.

#### F-03 Logging va error handling
- Priority: P1
- User Story: La team dev, toi muon theo doi loi de debug nhanh.
- Acceptance Criteria:
  - Co global exception handling
  - Log co trace-id (neu co) va thong diep ro rang
- Micro-functions derived from "I want":
  - Configure global exception handler, map to friendly responses, log stack traces
  - Include consistent request identifiers in logs to correlate tracing information
- Clarifications / redundancy check: Avoid duplicate try/catch in controllers if global handler is in place; use consistent logging helper.

#### F-04 Quan ly cau hinh theo moi truong
- Priority: P2
- User Story: La team devops, toi muon tach cau hinh local/staging/prod.
- Acceptance Criteria:
  - Dung profile Spring (`dev`, `staging`, `prod`)
  - Secret khong hardcode trong source
- Micro-functions derived from "I want":
  - Define Spring profiles with environment-specific properties for DB, payment, logging
  - Load secrets from env vars/keystore, refuse to start if missing
- Clarifications / redundancy check: Avoid duplicating environment logic in code; let Spring profile handling manage differences.

## 4. Release Proposal (3 Sprints)

### Sprint 1 (MVP Core - 2 tuan)
- A-01, A-02, B-01, B-02, B-03
- C-01, C-02, C-03
- E-03 (muc co ban), E-06 (muc co ban)
- F-02

### Sprint 2 (Business Completion - 2 tuan)
- C-04, D-01
- E-04, E-05, E-07
- E-01, E-02
- F-01 (dot 1)

### Sprint 3 (Hardening & Enhancements - 2 tuan)
- A-03, A-04
- D-02, D-03
- F-01 (dot 2), F-03, F-04

## 5. Definition of Done (DoD)
- Code review dat yeu cau
- Co test cho logic moi (unit/integration tuy scope)
- Khong pha vo cac luong user/admin hien co
- UI render dung o desktop va mobile co ban
- Khong hardcode secret, khong log lo thong tin nhay cam
- Tai lieu lien quan duoc cap nhat

## 6. Known Risks
- Ton kho co the sai lech neu dat hang dong thoi cao
- Voucher race condition neu khong lock transaction
- Bao cao native SQL phu thuoc chat vao schema
- Session-based auth chua toi uu cho scale ngang
