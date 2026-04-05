# Chuyen du lieu sang PostgreSQL

## Muc tieu

- App hien da mac dinh dung PostgreSQL.
- Schema moi dung ten bang `DoChoi` va `BienTheDoChoi`.
- Viec con lai la tao bang dung chuan va nap du lieu.

## Cau hinh ket noi mac dinh

- DB URL: `jdbc:postgresql://localhost:5432/webmohinh2`
- Username mac dinh: `postgres`
- Driver: `org.postgresql.Driver`

Co the doi bang env trong file [`.env.example`](/d:/Shop Gia Thinh web/.env.example).

## Truong hop 1: Database dang trang

Day la cach dung cho Supabase/PostgreSQL moi tao.

1. Chay [`docs/POSTGRES_BASE_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_BASE_SCHEMA.sql).
2. Khong can chay `POSTGRES_PRODUCT_SCHEMA.sql` neu ban dung schema moi tu dau.
3. Tach Excel thanh CSV UTF-8 theo bang:
   - `DanhMuc`
   - `HangSanXuat`
   - `DoChoi`
   - `BienTheDoChoi`
   - `TaiKhoan`
   - `DonHang`
   - `ChiTietDonHang`
4. Import theo thu tu khoa ngoai:
   - `VaiTro`
   - `TaiKhoan`
   - `DanhMuc`
   - `HangSanXuat`
   - `DoChoi`
   - `BienTheDoChoi`
   - `DonHang`
   - `ChiTietDonHang`
5. Kiem tra lai gia tri ID neu file CSV co cot identity.

## Truong hop 2: Ban da lo tao schema cu voi `MoHinh`

1. Chay [`docs/POSTGRES_RENAME_TO_TOY_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_RENAME_TO_TOY_SCHEMA.sql).
2. Chay tiep [`docs/POSTGRES_PRODUCT_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_PRODUCT_SCHEMA.sql) neu bang cu thieu cac cot catalog moi.
3. Kiem tra lai bang:
   - `DoChoi`
   - `BienTheDoChoi`
   - `ChiTietDonHang`
   - `ChiTietNhap`
   - `GioHang`
   - `YeuThich`
   - `DanhGia`

## Truong hop 3: Ban dang co SQL Server va muon chuyen ca du lieu cu

### Cach de lam nhat

- Dung DBeaver:
  - Connect SQL Server
  - Connect PostgreSQL
  - Chuyen bang qua Data Transfer

### Cach nhanh cho nhieu bang

- Dung `pgloader` neu ban co schema/data tu SQL Server.

Vi du:
```lisp
LOAD DATABASE
     FROM mssql://sa:password@localhost/WebMoHinh2
     INTO postgresql://postgres:password@localhost/webmohinh2
 WITH include drop, create tables, create indexes, reset sequences;
```

Sau do:
- doi chieu ten bang/cot sang schema moi `DoChoi`
- kiem tra khoa ngoai
- test dang nhap admin, do choi, don hang, dashboard

## Luu y quan trong

- Script trong `docs/migrate_*.sql` la script cu cho SQL Server, khong dung truc tiep cho PostgreSQL.
- File [`docs/POSTGRES_BASE_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_BASE_SCHEMA.sql) dung cho database moi.
- File [`docs/POSTGRES_RENAME_TO_TOY_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_RENAME_TO_TOY_SCHEMA.sql) dung de doi schema cu `MoHinh` sang schema moi `DoChoi`.
- File [`docs/POSTGRES_PRODUCT_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_PRODUCT_SCHEMA.sql) chi dung cho bang da ton tai, khong phai file tao bang tu dau.
- App da duoc sua de bao cao thang chay voi PostgreSQL.
- Truong `MoTa` cua bang `DoChoi` dung `TEXT`, khong con phu thuoc `NVARCHAR(MAX)`.

## Kiem tra sau migrate

1. Dang nhap admin duoc.
2. Trang danh sach do choi hien du lieu.
3. Trang admin do choi tao/sua duoc.
4. Dashboard va danh sach don hang load duoc.
5. Popup lien he dat hang tren frontend hien so dien thoai/Zalo dung.
