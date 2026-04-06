package com.example.asm.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OrderSchemaBootstrapConfig {

    @Bean
    @Order(2)
    CommandLineRunner orderSchemaRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("""
                    ALTER TABLE DonHang
                    ADD COLUMN IF NOT EXISTS TenNguoiNhan VARCHAR(255)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE DonHang
                    ADD COLUMN IF NOT EXISTS SoDienThoaiNhan VARCHAR(20)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE DonHang
                    ADD COLUMN IF NOT EXISTS EmailNguoiNhan VARCHAR(100)
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE DonHang
                    ADD COLUMN IF NOT EXISTS GhiChu VARCHAR(1000)
                    """);

            jdbcTemplate.execute("""
                    UPDATE DonHang dh
                    SET TenNguoiNhan = tk.HoTen
                    FROM TaiKhoan tk
                    WHERE dh.MaTaiKhoan = tk.MaTaiKhoan
                      AND (dh.TenNguoiNhan IS NULL OR dh.TenNguoiNhan = '')
                    """);
            jdbcTemplate.execute("""
                    UPDATE DonHang dh
                    SET SoDienThoaiNhan = tk.SoDienThoai
                    FROM TaiKhoan tk
                    WHERE dh.MaTaiKhoan = tk.MaTaiKhoan
                      AND (dh.SoDienThoaiNhan IS NULL OR dh.SoDienThoaiNhan = '')
                    """);
            jdbcTemplate.execute("""
                    UPDATE DonHang dh
                    SET EmailNguoiNhan = tk.Email
                    FROM TaiKhoan tk
                    WHERE dh.MaTaiKhoan = tk.MaTaiKhoan
                      AND (dh.EmailNguoiNhan IS NULL OR dh.EmailNguoiNhan = '')
                    """);
        };
    }
}
