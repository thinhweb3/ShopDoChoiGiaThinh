package com.example.asm.dto;

/**
 * DTO cho thống kê tổng quan dashboard
 */
public record ThongKeTongQuanDTO(
    Long tongDoanhThu,
    Long tongDonHang,
    Long tongSanPhamBan,
    Long tongKhachHang,
    Double tyLeTangTruong,
    Long donHangChoXacNhan,
    Long donHangDangGiao,
    Long donHangHoanThanh
) {}
