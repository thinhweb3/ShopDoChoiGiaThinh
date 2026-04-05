package com.example.asm.dto;

/**
 * DTO cho thống kê doanh thu theo danh mục
 */
public record ThongKeDanhMucDTO(
    String tenDanhMuc,
    Long tongDoanhThu,
    Long tongSoLuong,
    Long giaCaoNhat,
    Long giaThapNhat,
    Long giaTrungBinh
) {}
