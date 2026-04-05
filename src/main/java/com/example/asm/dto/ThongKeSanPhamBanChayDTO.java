package com.example.asm.dto;

/**
 * DTO cho thống kê sản phẩm bán chạy
 */
public record ThongKeSanPhamBanChayDTO(
    String maMoHinh,
    String tenMoHinh,
    String tenDanhMuc,
    Long tongSoLuong,
    Long tongDoanhThu,
    Double giaTrungBinh
) {}
