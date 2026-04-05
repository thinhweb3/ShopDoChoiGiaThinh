package com.example.asm.dto;

public record AdminRecentOrderItemDTO(
        Integer maDonHang,
        String customerName,
        Long tongTien,
        String orderStatus,
        String paymentStatus
) {
}
