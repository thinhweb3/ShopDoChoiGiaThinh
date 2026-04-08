package com.example.asm.config;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

@Configuration
public class IdentitySequenceBootstrapConfig {

    private static final List<IdentityColumn> IDENTITY_COLUMNS = List.of(
            new IdentityColumn("VaiTro", "MaVaiTro"),
            new IdentityColumn("TaiKhoan", "MaTaiKhoan"),
            new IdentityColumn("DanhMuc", "MaDanhMuc"),
            new IdentityColumn("HangSanXuat", "MaHang"),
            new IdentityColumn("LoaiHang", "MaLoaiHang"),
            new IdentityColumn("BienTheDoChoi", "MaBienTheDoChoi"),
            new IdentityColumn("DonHang", "MaDonHang"),
            new IdentityColumn("ChiTietDonHang", "MaChiTiet"),
            new IdentityColumn("DonNhap", "MaDonNhap"),
            new IdentityColumn("ChiTietNhap", "MaChiTietNhap"),
            new IdentityColumn("GioHang", "MaGioHang"),
            new IdentityColumn("YeuThich", "MaYeuThich"),
            new IdentityColumn("DanhGia", "MaDanhGia")
    );

    @Bean
    @Order(3)
    CommandLineRunner identitySequenceRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            for (IdentityColumn identityColumn : IDENTITY_COLUMNS) {
                syncIdentitySequence(jdbcTemplate, identityColumn);
            }
        };
    }

    private void syncIdentitySequence(JdbcTemplate jdbcTemplate, IdentityColumn identityColumn) {
        String tableName = identityColumn.tableName();
        String columnName = identityColumn.columnName();
        String normalizedTableName = tableName.toLowerCase(Locale.ROOT);
        String normalizedColumnName = columnName.toLowerCase(Locale.ROOT);

        Integer columnCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE lower(table_name) = ?
                  AND lower(column_name) = ?
                """, Integer.class, normalizedTableName, normalizedColumnName);

        if (columnCount == null || columnCount == 0) {
            return;
        }

        String sequenceName = jdbcTemplate.queryForObject(
                "SELECT pg_get_serial_sequence(?, ?)",
                String.class,
                normalizedTableName,
                normalizedColumnName);
        if (!StringUtils.hasText(sequenceName)) {
            return;
        }

        Long maxId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(%s), 0) FROM %s".formatted(columnName, tableName),
                Long.class);
        Long lastSequenceValue = jdbcTemplate.queryForObject(
                "SELECT last_value FROM " + quoteQualifiedIdentifier(sequenceName),
                Long.class);

        if (maxId != null && (lastSequenceValue == null || lastSequenceValue <= maxId)) {
            jdbcTemplate.queryForObject(
                    "SELECT setval(?::regclass, ?, false)",
                    Long.class,
                    sequenceName,
                    maxId + 1);
        }
    }

    private String quoteQualifiedIdentifier(String qualifiedIdentifier) {
        return Stream.of(qualifiedIdentifier.split("\\."))
                .map(part -> "\"" + part.replace("\"", "\"\"") + "\"")
                .collect(Collectors.joining("."));
    }

    private record IdentityColumn(String tableName, String columnName) {
    }
}
