package com.example.asm.controller.admin;

import com.example.asm.dto.ThongKeTongQuanDTO;
import com.example.asm.service.AdminDashboardService;
import com.example.asm.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller xử lý trang Dashboard admin
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("@adminPermissionGuard.hasAnyAdminPermission()")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    @Autowired
    private ReportService reportService;

    /**
     * Trang Dashboard chính - Tổng hợp thống kê
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now;
        LocalDateTime startDate = now.toLocalDate().minusDays(30).atTime(LocalTime.MIN);

        ThongKeTongQuanDTO tongQuan = new ThongKeTongQuanDTO(
                0L,
                dashboardService.getTotalOrders(),
                0L,
                0L,
                0.0,
                0L,
                0L,
                0L
        );
        List<Object[]> doanhThuTheoThang = List.of();

        try {
            tongQuan = reportService.thongKeTongQuan(startDate, endDate);
            doanhThuTheoThang = reportService.thongKeDoanhThuTheoThang(now.getYear());
        } catch (RuntimeException ex) {
            model.addAttribute("dashboardWarning", "Không tải được đầy đủ báo cáo. Trang điều khiển đang hiển thị dữ liệu cơ bản.");
        }

        prepareChartData(model, doanhThuTheoThang);

        model.addAttribute("tongQuan", tongQuan);
        model.addAttribute("totalOrders", dashboardService.getTotalOrders());
        model.addAttribute("totalProducts", dashboardService.getTotalProducts());
        model.addAttribute("lowStockProducts", dashboardService.getLowStockProducts());
        model.addAttribute("recentOrderItems", dashboardService.getRecentOrderItems());
        model.addAttribute("cancelledOrders", dashboardService.getCancelledOrders());

        return "admin/dashboard";
    }

    /**
     * Chuẩn bị dữ liệu cho biểu đồ
     */
    private void prepareChartData(Model model, List<Object[]> monthlyData) {
        // Tạo mảng 12 tháng với giá trị mặc định 0
        Long[] revenueData = new Long[12];
        Long[] orderData = new Long[12];
        for (int i = 0; i < 12; i++) {
            revenueData[i] = 0L;
            orderData[i] = 0L;
        }

        // Điền dữ liệu từ database
        if (monthlyData != null) {
            for (Object[] row : monthlyData) {
                int month = ((Number) row[0]).intValue() - 1; // 0-indexed
                if (month >= 0 && month < 12) {
                    revenueData[month] = ((Number) row[1]).longValue();
                    orderData[month] = ((Number) row[2]).longValue();
                }
            }
        }

        // Chuyển đổi sang string cho JavaScript
        StringBuilder labels = new StringBuilder("['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']");
        StringBuilder revenues = new StringBuilder("[");
        StringBuilder orders = new StringBuilder("[");

        for (int i = 0; i < 12; i++) {
            revenues.append(revenueData[i]);
            orders.append(orderData[i]);
            if (i < 11) {
                revenues.append(", ");
                orders.append(", ");
            }
        }
        revenues.append("]");
        orders.append("]");

        model.addAttribute("chartLabels", labels.toString());
        model.addAttribute("chartRevenues", revenues.toString());
        model.addAttribute("chartOrders", orders.toString());
    }
}
