-- Dung khi ban da lo tao schema cu voi ten MoHinh / BienTheMoHinh.
-- Chay file nay truoc, sau do moi chay POSTGRES_PRODUCT_SCHEMA.sql neu can them cot.

DO $$
BEGIN
    IF to_regclass('"MoHinh"') IS NOT NULL AND to_regclass('dochoi') IS NULL THEN
        EXECUTE 'ALTER TABLE "MoHinh" RENAME TO DoChoi';
    ELSIF to_regclass('mohinh') IS NOT NULL AND to_regclass('dochoi') IS NULL THEN
        EXECUTE 'ALTER TABLE MoHinh RENAME TO DoChoi';
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('"BienTheMoHinh"') IS NOT NULL AND to_regclass('bienthedochoi') IS NULL THEN
        EXECUTE 'ALTER TABLE "BienTheMoHinh" RENAME TO BienTheDoChoi';
    ELSIF to_regclass('bienthemohinh') IS NOT NULL AND to_regclass('bienthedochoi') IS NULL THEN
        EXECUTE 'ALTER TABLE BienTheMoHinh RENAME TO BienTheDoChoi';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('DoChoi')
          AND lower(column_name) = lower('MaMoHinh')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE DoChoi RENAME COLUMN "MaMoHinh" TO MaDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE DoChoi RENAME COLUMN MaMoHinh TO MaDoChoi';
        END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('DoChoi')
          AND lower(column_name) = lower('TenMoHinh')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE DoChoi RENAME COLUMN "TenMoHinh" TO TenDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE DoChoi RENAME COLUMN TenMoHinh TO TenDoChoi';
        END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('BienTheDoChoi')
          AND lower(column_name) = lower('MaBienThe')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE BienTheDoChoi RENAME COLUMN "MaBienThe" TO MaBienTheDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE BienTheDoChoi RENAME COLUMN MaBienThe TO MaBienTheDoChoi';
        END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('BienTheDoChoi')
          AND lower(column_name) = lower('MaMoHinh')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE BienTheDoChoi RENAME COLUMN "MaMoHinh" TO MaDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE BienTheDoChoi RENAME COLUMN MaMoHinh TO MaDoChoi';
        END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('ChiTietDonHang')
          AND lower(column_name) = lower('MaBienThe')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE ChiTietDonHang RENAME COLUMN "MaBienThe" TO MaBienTheDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE ChiTietDonHang RENAME COLUMN MaBienThe TO MaBienTheDoChoi';
        END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('ChiTietNhap')
          AND lower(column_name) = lower('MaBienThe')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE ChiTietNhap RENAME COLUMN "MaBienThe" TO MaBienTheDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE ChiTietNhap RENAME COLUMN MaBienThe TO MaBienTheDoChoi';
        END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('GioHang')
          AND lower(column_name) = lower('MaBienThe')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE GioHang RENAME COLUMN "MaBienThe" TO MaBienTheDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE GioHang RENAME COLUMN MaBienThe TO MaBienTheDoChoi';
        END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('YeuThich')
          AND lower(column_name) = lower('MaMoHinh')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE YeuThich RENAME COLUMN "MaMoHinh" TO MaDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE YeuThich RENAME COLUMN MaMoHinh TO MaDoChoi';
        END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE lower(table_name) = lower('DanhGia')
          AND lower(column_name) = lower('MaMoHinh')
    ) THEN
        BEGIN
            EXECUTE 'ALTER TABLE DanhGia RENAME COLUMN "MaMoHinh" TO MaDoChoi';
        EXCEPTION WHEN undefined_column THEN
            EXECUTE 'ALTER TABLE DanhGia RENAME COLUMN MaMoHinh TO MaDoChoi';
        END;
    END IF;
END $$;
