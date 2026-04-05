IF COL_LENGTH('dbo.VaiTro', 'Quyen') IS NULL
BEGIN
    ALTER TABLE dbo.VaiTro
    ADD Quyen NVARCHAR(2000) NULL;
END
GO

UPDATE dbo.VaiTro
SET Quyen = ''
WHERE Quyen IS NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.default_constraints dc
    INNER JOIN sys.columns c
        ON c.default_object_id = dc.object_id
    INNER JOIN sys.tables t
        ON t.object_id = c.object_id
    WHERE t.name = 'VaiTro'
      AND c.name = 'Quyen'
)
BEGIN
    ALTER TABLE dbo.VaiTro
    ADD CONSTRAINT DF_VaiTro_Quyen DEFAULT N'' FOR Quyen;
END
GO
