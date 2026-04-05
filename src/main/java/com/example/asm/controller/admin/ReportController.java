package com.example.asm.controller.admin;

import com.example.asm.dto.ThongKeDanhMucDTO;
import com.example.asm.dto.ThongKeKhachHangVIPDTO;
import com.example.asm.dto.ThongKeSanPhamBanChayDTO;
import com.example.asm.dto.ThongKeTongQuanDTO;
import com.example.asm.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("@adminPermissionGuard.hasAnyAdminPermission()")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/reports")
    public String reports(Model model,
                         @RequestParam(required = false) String startDate,
                         @RequestParam(required = false) String endDate) {

        LocalDate endLocalDate = parseDate(endDate, LocalDate.now());
        LocalDate startLocalDate = parseDate(startDate, endLocalDate.minusDays(30));
        if (startLocalDate.isAfter(endLocalDate)) {
            startLocalDate = endLocalDate.minusDays(30);
        }

        LocalDateTime start = startLocalDate.atTime(LocalTime.MIN);
        LocalDateTime end = endLocalDate.atTime(LocalTime.MAX);

        ThongKeTongQuanDTO tongQuan = reportService.thongKeTongQuan(start, end);
        model.addAttribute("tongQuan", tongQuan);

        List<ThongKeDanhMucDTO> thongKeDanhMuc = reportService.thongKeDoanhThuTheoDanhMuc(start, end);
        model.addAttribute("thongKeDanhMuc", thongKeDanhMuc);

        List<ThongKeKhachHangVIPDTO> khachHangVIP = reportService.thongKeKhachHangVIP(start, end);
        model.addAttribute("khachHangVIP", khachHangVIP);

        List<ThongKeSanPhamBanChayDTO> sanPhamBanChay = reportService.thongKeSanPhamBanChay(start, end, 10);
        model.addAttribute("sanPhamBanChay", sanPhamBanChay);

        List<Object[]> doanhThuTheoThang = reportService.thongKeDoanhThuTheoThang(end.getYear());
        model.addAttribute("doanhThuTheoThang", doanhThuTheoThang);

        model.addAttribute("startDate", startLocalDate.toString());
        model.addAttribute("endDate", endLocalDate.toString());
        model.addAttribute("currentYear", end.getYear());

        return "admin/reports";
    }

    private LocalDate parseDate(String rawDate, LocalDate fallback) {
        if (rawDate == null || rawDate.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(rawDate);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
