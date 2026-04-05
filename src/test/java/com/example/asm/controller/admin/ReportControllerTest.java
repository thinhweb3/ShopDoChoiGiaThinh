package com.example.asm.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.asm.dto.ThongKeDanhMucDTO;
import com.example.asm.dto.ThongKeKhachHangVIPDTO;
import com.example.asm.dto.ThongKeSanPhamBanChayDTO;
import com.example.asm.dto.ThongKeTongQuanDTO;
import com.example.asm.service.ReportService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class ReportControllerTest {

    @Test
    void reportsShouldUseProvidedDatesAndPopulateModel() {
        ReportService reportService = org.mockito.Mockito.mock(ReportService.class);
        ReportController controller = new ReportController();
        ReflectionTestUtils.setField(controller, "reportService", reportService);

        ThongKeTongQuanDTO tongQuan = new ThongKeTongQuanDTO(100L, 2L, 3L, 1L, 10.0, 0L, 0L, 0L);
        List<ThongKeDanhMucDTO> danhMuc = List.of(new ThongKeDanhMucDTO("Figure", 100L, 2L, 60L, 40L, 50L));
        List<ThongKeKhachHangVIPDTO> vip = List.of(new ThongKeKhachHangVIPDTO(1, "A", "a@mail.com", 2L, 100L, 50.0));
        List<ThongKeSanPhamBanChayDTO> top = List.of(new ThongKeSanPhamBanChayDTO("MH01", "RX-78", "Figure", 3L, 150L, 50.0));
        List<Object[]> byMonth = new ArrayList<>();
        byMonth.add(new Object[] {1, 100L, 2L});

        when(reportService.thongKeTongQuan(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(tongQuan);
        when(reportService.thongKeDoanhThuTheoDanhMuc(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(danhMuc);
        when(reportService.thongKeKhachHangVIP(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(vip);
        when(reportService.thongKeSanPhamBanChay(any(LocalDateTime.class), any(LocalDateTime.class), eq(10))).thenReturn(top);
        when(reportService.thongKeDoanhThuTheoThang(eq(2026))).thenReturn(byMonth);

        Model model = new ExtendedModelMap();
        String view = controller.reports(model, "2026-01-01", "2026-01-31");

        assertThat(view).isEqualTo("admin/reports");
        assertThat(model.getAttribute("tongQuan")).isEqualTo(tongQuan);
        assertThat(model.getAttribute("thongKeDanhMuc")).isEqualTo(danhMuc);
        assertThat(model.getAttribute("khachHangVIP")).isEqualTo(vip);
        assertThat(model.getAttribute("sanPhamBanChay")).isEqualTo(top);
        assertThat(model.getAttribute("doanhThuTheoThang")).isEqualTo(byMonth);
        assertThat(model.getAttribute("startDate")).isEqualTo("2026-01-01");
        assertThat(model.getAttribute("endDate")).isEqualTo("2026-01-31");
        assertThat(model.getAttribute("currentYear")).isEqualTo(2026);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(reportService).thongKeTongQuan(startCaptor.capture(), endCaptor.capture());
        assertThat(startCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0, 0));
        assertThat(endCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 1, 31, 23, 59, 59, 999_999_999));
    }

    @Test
    void reportsShouldUseDefaultDatesWhenInputIsBlank() {
        ReportService reportService = org.mockito.Mockito.mock(ReportService.class);
        ReportController controller = new ReportController();
        ReflectionTestUtils.setField(controller, "reportService", reportService);

        when(reportService.thongKeTongQuan(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ThongKeTongQuanDTO(0L, 0L, 0L, 0L, 0.0, 0L, 0L, 0L));
        when(reportService.thongKeDoanhThuTheoDanhMuc(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(reportService.thongKeKhachHangVIP(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(reportService.thongKeSanPhamBanChay(any(LocalDateTime.class), any(LocalDateTime.class), eq(10))).thenReturn(List.of());
        when(reportService.thongKeDoanhThuTheoThang(any(Integer.class))).thenReturn(List.of());

        LocalDate today = LocalDate.now();
        Model model = new ExtendedModelMap();
        controller.reports(model, " ", "");

        assertThat(model.getAttribute("startDate")).isEqualTo(today.minusDays(30).toString());
        assertThat(model.getAttribute("endDate")).isEqualTo(today.toString());
        assertThat(model.getAttribute("currentYear")).isEqualTo(today.getYear());
    }

    @Test
    void reportsShouldFallbackWhenDateIsInvalid() {
        ReportService reportService = org.mockito.Mockito.mock(ReportService.class);
        ReportController controller = new ReportController();
        ReflectionTestUtils.setField(controller, "reportService", reportService);

        when(reportService.thongKeTongQuan(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ThongKeTongQuanDTO(0L, 0L, 0L, 0L, 0.0, 0L, 0L, 0L));
        when(reportService.thongKeDoanhThuTheoDanhMuc(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(reportService.thongKeKhachHangVIP(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(reportService.thongKeSanPhamBanChay(any(LocalDateTime.class), any(LocalDateTime.class), eq(10))).thenReturn(List.of());
        when(reportService.thongKeDoanhThuTheoThang(any(Integer.class))).thenReturn(List.of());

        LocalDate expectedEnd = LocalDate.of(2026, 2, 20);
        LocalDate expectedStart = expectedEnd.minusDays(30);
        Model model = new ExtendedModelMap();
        controller.reports(model, "not-a-date", "2026-02-20");

        assertThat(model.getAttribute("startDate")).isEqualTo(expectedStart.toString());
        assertThat(model.getAttribute("endDate")).isEqualTo(expectedEnd.toString());
        assertThat(model.getAttribute("currentYear")).isEqualTo(2026);
    }

    @Test
    void reportsShouldResetStartDateWhenAfterEndDate() {
        ReportService reportService = org.mockito.Mockito.mock(ReportService.class);
        ReportController controller = new ReportController();
        ReflectionTestUtils.setField(controller, "reportService", reportService);

        when(reportService.thongKeTongQuan(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ThongKeTongQuanDTO(0L, 0L, 0L, 0L, 0.0, 0L, 0L, 0L));
        when(reportService.thongKeDoanhThuTheoDanhMuc(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(reportService.thongKeKhachHangVIP(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(reportService.thongKeSanPhamBanChay(any(LocalDateTime.class), any(LocalDateTime.class), eq(10))).thenReturn(List.of());
        when(reportService.thongKeDoanhThuTheoThang(eq(2026))).thenReturn(List.of());

        LocalDate endDate = LocalDate.of(2026, 1, 10);
        LocalDate correctedStartDate = endDate.minusDays(30);
        Model model = new ExtendedModelMap();
        controller.reports(model, "2026-02-01", "2026-01-10");

        assertThat(model.getAttribute("startDate")).isEqualTo(correctedStartDate.toString());
        assertThat(model.getAttribute("endDate")).isEqualTo(endDate.toString());

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(reportService).thongKeTongQuan(startCaptor.capture(), endCaptor.capture());
        assertThat(startCaptor.getValue()).isEqualTo(correctedStartDate.atStartOfDay());
        assertThat(endCaptor.getValue()).isEqualTo(endDate.atTime(LocalTime.MAX));
    }
}
