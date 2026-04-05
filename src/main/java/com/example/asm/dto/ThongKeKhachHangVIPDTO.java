package com.example.asm.dto;

/**
 * DTO cho thống kê khách hàng VIP
 */
public record ThongKeKhachHangVIPDTO(
    Integer maTaiKhoan,
    String hoTen,
    String email,
    Long tongDonHang,
    Long tongChiTieu,
    Double giaTrungBinh
) {}
