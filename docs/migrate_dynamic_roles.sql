USE WebMoHinh2;
GO

IF OBJECT_ID('dbo.VaiTro', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.VaiTro (
        MaVaiTro INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        Code VARCHAR(50) NOT NULL,
        TenHienThi NVARCHAR(100) NOT NULL,
        MoTa NVARCHAR(255) NOT NULL,
        ChoPhepTruyCapAdmin BIT NOT NULL CONSTRAINT DF_VaiTro_AdminAccess DEFAULT 0,
        LaVaiTroHeThong BIT NOT NULL CONSTRAINT DF_VaiTro_SystemRole DEFAULT 0,
        CONSTRAINT UQ_VaiTro_Code UNIQUE (Code)
    );
END
GO

MERGE dbo.VaiTro AS target
USING (
    SELECT 'SUPER_ADMIN' AS Code, N'Super Admin' AS TenHienThi, N'Toàn quyền hệ thống' AS MoTa, CAST(1 AS BIT) AS ChoPhepTruyCapAdmin, CAST(1 AS BIT) AS LaVaiTroHeThong
    UNION ALL
    SELECT 'ADMIN', N'Admin', N'Quản trị nội dung và vận hành', CAST(1 AS BIT), CAST(1 AS BIT)
    UNION ALL
    SELECT 'USER', N'User', N'Khách hàng mua hàng', CAST(0 AS BIT), CAST(1 AS BIT)
) AS source
ON UPPER(target.Code) = source.Code
WHEN MATCHED THEN
    UPDATE SET
        target.TenHienThi = source.TenHienThi,
        target.MoTa = source.MoTa,
        target.ChoPhepTruyCapAdmin = source.ChoPhepTruyCapAdmin,
        target.LaVaiTroHeThong = source.LaVaiTroHeThong
WHEN NOT MATCHED THEN
    INSERT (Code, TenHienThi, MoTa, ChoPhepTruyCapAdmin, LaVaiTroHeThong)
    VALUES (source.Code, source.TenHienThi, source.MoTa, source.ChoPhepTruyCapAdmin, source.LaVaiTroHeThong);
GO

IF OBJECT_ID('dbo.TaiKhoanRole', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.TaiKhoanRole (
        MaTaiKhoan INT NOT NULL,
        MaVaiTro INT NOT NULL,
        CONSTRAINT PK_TaiKhoanRole PRIMARY KEY (MaTaiKhoan, MaVaiTro),
        CONSTRAINT FK_TaiKhoanRole_TaiKhoan
            FOREIGN KEY (MaTaiKhoan) REFERENCES dbo.TaiKhoan(MaTaiKhoan)
            ON DELETE CASCADE,
        CONSTRAINT FK_TaiKhoanRole_VaiTro
            FOREIGN KEY (MaVaiTro) REFERENCES dbo.VaiTro(MaVaiTro)
            ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID('dbo.TaiKhoanRole', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.TaiKhoanRole', 'Role') IS NOT NULL
BEGIN
    DECLARE @migrateTaiKhoanRoleSql NVARCHAR(MAX) = N'';

    IF OBJECT_ID('dbo.TaiKhoanRole_New', 'U') IS NOT NULL
        DROP TABLE dbo.TaiKhoanRole_New;

    CREATE TABLE dbo.TaiKhoanRole_New (
        MaTaiKhoan INT NOT NULL,
        MaVaiTro INT NOT NULL,
        CONSTRAINT PK_TaiKhoanRole_New PRIMARY KEY (MaTaiKhoan, MaVaiTro),
        CONSTRAINT FK_TaiKhoanRole_New_TaiKhoan
            FOREIGN KEY (MaTaiKhoan) REFERENCES dbo.TaiKhoan(MaTaiKhoan)
            ON DELETE CASCADE,
        CONSTRAINT FK_TaiKhoanRole_New_VaiTro
            FOREIGN KEY (MaVaiTro) REFERENCES dbo.VaiTro(MaVaiTro)
            ON DELETE CASCADE
    );

    SET @migrateTaiKhoanRoleSql = N'
        INSERT INTO dbo.TaiKhoanRole_New (MaTaiKhoan, MaVaiTro)
        SELECT DISTINCT oldRole.MaTaiKhoan, roleDef.MaVaiTro
        FROM dbo.TaiKhoanRole oldRole
        INNER JOIN dbo.VaiTro roleDef
            ON roleDef.Code = CASE oldRole.Role
                WHEN 0 THEN ''SUPER_ADMIN''
                WHEN 1 THEN ''ADMIN''
                ELSE ''USER''
            END;';
    EXEC sp_executesql @migrateTaiKhoanRoleSql;

    DROP TABLE dbo.TaiKhoanRole;
    EXEC sp_rename 'dbo.TaiKhoanRole_New', 'TaiKhoanRole';
END
GO

IF COL_LENGTH('dbo.TaiKhoan', 'Role') IS NOT NULL
BEGIN
    DECLARE @sql NVARCHAR(MAX) = N'';
    DECLARE @migrateTaiKhoanSql NVARCHAR(MAX) = N'';

    SET @migrateTaiKhoanSql = N'
        INSERT INTO dbo.TaiKhoanRole (MaTaiKhoan, MaVaiTro)
        SELECT DISTINCT tk.MaTaiKhoan, roleDef.MaVaiTro
        FROM dbo.TaiKhoan tk
        INNER JOIN dbo.VaiTro roleDef
            ON roleDef.Code = CASE tk.Role
                WHEN 0 THEN ''SUPER_ADMIN''
                WHEN 1 THEN ''ADMIN''
                ELSE ''USER''
            END
        WHERE NOT EXISTS (
            SELECT 1
            FROM dbo.TaiKhoanRole tr
            WHERE tr.MaTaiKhoan = tk.MaTaiKhoan
              AND tr.MaVaiTro = roleDef.MaVaiTro
        );';
    EXEC sp_executesql @migrateTaiKhoanSql;

    SELECT @sql = @sql + N'ALTER TABLE dbo.TaiKhoan DROP CONSTRAINT [' + dc.name + N'];'
    FROM sys.default_constraints dc
    INNER JOIN sys.columns c
        ON c.object_id = dc.parent_object_id
       AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID('dbo.TaiKhoan')
      AND c.name = 'Role';

    IF @sql <> N''
        EXEC sp_executesql @sql;

    ALTER TABLE dbo.TaiKhoan DROP COLUMN Role;
END
GO

INSERT INTO dbo.TaiKhoanRole (MaTaiKhoan, MaVaiTro)
SELECT tk.MaTaiKhoan, userRole.MaVaiTro
FROM dbo.TaiKhoan tk
CROSS JOIN (
    SELECT TOP 1 MaVaiTro
    FROM dbo.VaiTro
    WHERE UPPER(Code) = 'USER'
) AS userRole
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.TaiKhoanRole tr
    WHERE tr.MaTaiKhoan = tk.MaTaiKhoan
);
GO

CREATE OR ALTER PROCEDURE dbo.Sp_ThongKeKhachHangVIP
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP 10
        TK.HoTen,
        TK.Email,
        SUM(D.TongTien) AS TongTienDaMua,
        MIN(D.NgayDat) AS NgayMuaDauTien,
        MAX(D.NgayDat) AS NgayMuaSauCung
    FROM dbo.TaiKhoan TK
    INNER JOIN dbo.TaiKhoanRole TKR
        ON TKR.MaTaiKhoan = TK.MaTaiKhoan
    INNER JOIN dbo.VaiTro VT
        ON VT.MaVaiTro = TKR.MaVaiTro
       AND UPPER(VT.Code) = 'USER'
    INNER JOIN dbo.DonHang D
        ON D.MaTaiKhoan = TK.MaTaiKhoan
    WHERE D.TrangThai = N'Hoàn thành'
    GROUP BY TK.HoTen, TK.Email
    ORDER BY TongTienDaMua DESC;
END
GO
