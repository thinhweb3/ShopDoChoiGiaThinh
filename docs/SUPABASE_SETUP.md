# Ket noi project voi Supabase

## Khi nao nen dung

- Ban chua cai PostgreSQL local.
- Ban muon co database online de deploy ngay.
- Ban muon dung chung 1 database cho local va server.

## Kieu ket noi nen dung cho project nay

Voi app Spring Boot chay lau dai, nen uu tien `Session pooler` cua Supabase.

Ly do:
- Project nay la backend persistent, khong phai serverless.
- Session pooler ho tro ca IPv4 va IPv6.
- Khong can tat prepared statements nhu transaction pooler.

## Cach lay thong tin ket noi

1. Vao Supabase dashboard.
2. Mo project cua ban.
3. Chon `Connect`.
4. Lay connection string o muc `Session pooler`.

Dang chuoi ket noi JDBC can dien vao env:

```env
DB_URL=jdbc:postgresql://aws-0-REGION.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.PROJECT_REF
DB_PASSWORD=SUPABASE_DB_PASSWORD
DB_DRIVER=org.postgresql.Driver
DB_MAX_POOL_SIZE=2
DB_MIN_IDLE=0
```

## Giai thich nhanh

- `REGION`: khu vuc project Supabase cua ban, vi du `ap-southeast-1`.
- `PROJECT_REF`: ma project Supabase.
- `SUPABASE_DB_PASSWORD`: database password ban dat luc tao project.

## Khong can cai PostgreSQL local

Ban chi can:
- Supabase project
- file `.env`
- app Spring Boot

La co the chay local truc tiep vao DB online.

## Sau khi ket noi xong

1. Neu project Supabase dang trang, chay [`docs/POSTGRES_BASE_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_BASE_SCHEMA.sql).
2. Neu ban da lo tao schema cu `MoHinh`, chay them [`docs/POSTGRES_RENAME_TO_TOY_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_RENAME_TO_TOY_SCHEMA.sql), sau do moi den [`docs/POSTGRES_PRODUCT_SCHEMA.sql`](/d:/Shop Gia Thinh web/docs/POSTGRES_PRODUCT_SCHEMA.sql).
3. Import du lieu tu Excel vao cac bang tren Supabase.
4. Kiem tra trang:
   - `/admin/dashboard`
   - `/admin/products`
   - `/product/list`

## Neu muon import Excel nhanh

- Excel -> CSV UTF-8
- Supabase Table Editor hoac DBeaver -> import CSV

Nhap theo thu tu:
- `DanhMuc`
- `HangSanXuat`
- `VaiTro`
- `TaiKhoan`
- `DoChoi`
- `BienTheDoChoi`
- `DonHang`
- `ChiTietDonHang`

## Neu bi loi ket noi

- Kiem tra `DB_URL` da co `sslmode=require` chua.
- Kiem tra `DB_USERNAME` co dang `postgres.PROJECT_REF` khong.
- Kiem tra password la database password, khong phai API key.
- Giu `DB_MAX_POOL_SIZE=2` cho Railway 1GB va Supabase free tier.
