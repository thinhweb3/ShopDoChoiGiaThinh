package com.example.asm.service;

import com.example.asm.dto.AdminRecentOrderItemDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.asm.entity.DonHang;
import com.example.asm.repository.ChiTietDonHangRepository;
import com.example.asm.repository.DonHangRepository;
import com.example.asm.repository.MoHinhRepository;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    @Autowired
    private ChiTietDonHangRepository chiTietRepo;

    @Autowired
    private DonHangRepository donHangRepo;

    @Autowired
    private MoHinhRepository moHinhRepo;

    // 2. Lấy tổng số đơn hàng
    public long getTotalOrders() {
        return donHangRepo.count();
    }

    // 3. Lấy số đơn hàng đã bị hủy
    public long getCancelledOrders() {
        return donHangRepo.countByTrangThai("Đã hủy"); 
    }

    public long getTotalProducts() {
        return moHinhRepo.count();
    }

    public long getLowStockProducts() {
        return moHinhRepo.findAll().stream()
                .filter(product -> product.getTonKho() != null)
                .filter(product -> product.getTonKhoToiThieu() != null)
                .filter(product -> product.getTonKho() <= product.getTonKhoToiThieu())
                .count();
    }

    // 4. Lấy danh sách 5 đơn hàng mới nhất
    public List<DonHang> getRecentOrders() {
        return donHangRepo.findTop5ByOrderByNgayDatDesc();
    }

    public List<AdminRecentOrderItemDTO> getRecentOrderItems() {
        return donHangRepo.findTop5ByOrderByNgayDatDesc().stream()
                .map(order -> new AdminRecentOrderItemDTO(
                        order.getMaDonHang(),
                        order.getTaiKhoan() != null && order.getTaiKhoan().getHoTen() != null
                                ? order.getTaiKhoan().getHoTen()
                                : "Khách lẻ",
                        order.getTongTien(),
                        toDisplayOrderStatus(order.getTrangThai()),
                        toDisplayPaymentStatus(order.getTrangThaiThanhToan())
                ))
                .toList();
    }

    // 6. Top sản phẩm bán chạy trong 90 ngày (Hàm bạn đã có)
    public List<Object[]> getTopSellingProductsLast90Days() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(90);
        return chiTietRepo.findTopSellingInLastDays(startDate, PageRequest.of(0, 5));
    }

    // 7. Xử lý dữ liệu doanh thu theo 12 tháng của năm chỉ định
    public List<Long> getMonthlyRevenueData(int year) {
        // Lấy dữ liệu thô từ DB: List<Object[]> gồm [Tháng, Tổng tiền]
        List<Object[]> rawData = donHangRepo.getMonthlyRevenue(year);

        // Khởi tạo mảng 12 phần tử có giá trị 0
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            result.add(0L);
        }

        // Map dữ liệu từ DB vào đúng vị trí tháng trong mảng
        if (rawData != null) {
            for (Object[] row : rawData) {
                // row[0] là Tháng (1-12), row[1] là Tổng tiền
                int month = ((Number) row[0]).intValue();
                Long revenue = ((Number) row[1]).longValue(); // Convert an toàn từ BigDecimal/Double/Long

                if (month >= 1 && month <= 12) {
                    result.set(month - 1, revenue); // Tháng 1 lưu vào index 0
                }
            }
        }
        return result;
    }

    private String toDisplayOrderStatus(String storedStatus) {
        if (storedStatus == null || storedStatus.isBlank()) {
            return "Đặt hàng";
        }
        if ("Chờ xử lý".equalsIgnoreCase(storedStatus)
                || "Đã xác nhận".equalsIgnoreCase(storedStatus)
                || "Đặt hàng".equalsIgnoreCase(storedStatus)) {
            return "Đặt hàng";
        }
        if ("Đang giao".equalsIgnoreCase(storedStatus)
                || "Đang giao hàng".equalsIgnoreCase(storedStatus)) {
            return "Đang giao";
        }
        if ("Hoàn thành".equalsIgnoreCase(storedStatus)
                || "Đã giao".equalsIgnoreCase(storedStatus)) {
            return "Đã giao";
        }
        return storedStatus;
    }

    private String toDisplayPaymentStatus(String storedStatus) {
        return "Đã thanh toán".equalsIgnoreCase(storedStatus)
                || "Đã nhận tiền".equalsIgnoreCase(storedStatus)
                ? "Đã nhận tiền"
                : "Chưa nhận tiền";
    }
}
