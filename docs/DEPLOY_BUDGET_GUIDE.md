# Deploy chi phi thap cho Shop Gia Thinh

## Tinh trang hien tai

- App da duoc tach secret ra bien moi truong.
- Upload anh san pham khong con ghi vao thu muc tam cua artifact.
- Frontend da duoc rut gon thanh catalog san pham + popup lien he dat hang.
- Public flow dang nhap, quen mat khau, review, cart va checkout da bi loai khoi giao dien khach.
- Giao dien da ho tro ca:
  - ten file local, vi du `gundam.jpg`
  - URL anh ngoai, vi du `https://res.cloudinary.com/...`

## Lua chon nen dung

### Phuong an it sua code nhat

- App: Railway hoac Render
- DB: PostgreSQL
- Anh: mount persistent disk va set `APP_STORAGE_UPLOAD_ROOT`

Project hien tai da duoc doi mac dinh sang PostgreSQL, nen huong nay vua hop codebase hien tai vua re hon SQL Server managed.

### Phuong an toi uu chi phi nhat

- App: Railway
- DB: Supabase Postgres
- Anh: Cloudinary Free hoac Supabase Storage
- Import du lieu: Excel -> CSV -> DBeaver/Supabase import

Huong dan ket noi truc tiep da duoc them o [`docs/SUPABASE_SETUP.md`](/d:/Shop Gia Thinh web/docs/SUPABASE_SETUP.md).

Luu y: code hien tai da mac dinh chay PostgreSQL. Neu ban dang co du lieu cu tren SQL Server thi can migrate/import du lieu sang Postgres.

## Bien moi truong can set

Danh sach day du nam trong file [`.env.example`](/d:/Shop Gia Thinh web/.env.example).

Toi thieu can co:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_STORAGE_UPLOAD_ROOT`
- `STORE_CONTACT_NAME`
- `STORE_CONTACT_PHONE`
- `STORE_CONTACT_ZALO`

## Cach xu ly anh toi uu chi phi

### Cach 1: Chua co anh

- Nhap san pham truoc.
- De `hinhAnh` trong DB rong hoac null.
- Giao dien tu dong roi ve anh mac dinh `banner-bg.jpg`.

### Cach 2: Upload file len server

- Dung form admin hien tai.
- Dat `APP_STORAGE_UPLOAD_ROOT=/data/uploads` neu deploy tren server/VPS co volume.
- Mapping `/images/**` se doc tu thu muc nay truoc, sau do moi fallback ve `classpath:/static/images/`.

### Cach 3: Dung Cloudinary/Supabase Storage

- Luu truc tiep URL anh vao cot `HinhAnh`.
- Giao dien hien tai da render duoc URL day du, khong can doi template nua.

## Workflow nhap du lieu tu Excel

### Neu ban dang co Excel va chua co DB moi

- Chuan hoa Excel thanh cac sheet hoac file CSV theo bang du lieu.
- Dung DBeaver Community hoac Supabase Table Editor de import CSV vao PostgreSQL.
- Neu database dang trang, chay [`docs/POSTGRES_BASE_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_BASE_SCHEMA.sql) truoc.
- Neu ban da lo tao schema cu `MoHinh`, chay them [`docs/POSTGRES_RENAME_TO_TOY_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_RENAME_TO_TOY_SCHEMA.sql) roi moi den [`docs/POSTGRES_PRODUCT_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_PRODUCT_SCHEMA.sql).
- Nhap danh muc, hang san xuat, do choi truoc.
- Anh co the bo sung sau trong trang admin.

### Neu ban migrate tu SQL Server hoac doi sang Supabase

- Tu Excel xuat ra CSV UTF-8.
- Import vao Supabase Table Editor hoac DBeaver.
- Kiem tra lai ma danh muc, ma hang, ma do choi de giu khoa ngoai dung.

## Viec can lam tiep theo neu muon host that su re

1. Rotate ngay toan bo secret cu vi truoc day da nam trong `application.properties`.
2. Tao PostgreSQL database moi va cap nhat env.
3. Import du lieu tu Excel hoac migrate du lieu cu tu SQL Server.
4. Neu dung upload local, gan persistent volume.
5. Neu khong muon ton tien cho volume, chuyen sang Cloudinary/Supabase Storage.
