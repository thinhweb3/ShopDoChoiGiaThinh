package com.example.asm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.asm.dto.ThongKeDanhMucDTO;
import com.example.asm.dto.ThongKeKhachHangVIPDTO;
import com.example.asm.dto.ThongKeSanPhamBanChayDTO;
import com.example.asm.dto.ThongKeTongQuanDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReportServiceTest {

    private ReportService reportService;
    private EntityManager em;

    @BeforeEach
    void setUp() {
        reportService = new ReportService();
        em = mock(EntityManager.class);
        ReflectionTestUtils.setField(reportService, "em", em);
    }

    @Test
    void thongKeTongQuanShouldCalculateGrowthAndPreviousPeriodCorrectly() {
        Query doanhThuQuery = mockSingleResultQuery(200L);
        Query tongDonQuery = mockSingleResultQuery(5L);
        Query tongSanPhamQuery = mockSingleResultQuery(12L);
        Query tongKhachHangQuery = mockSingleResultQuery(3L);
        Query doanhThuKyTruocQuery = mockSingleResultQuery(100L);

        when(em.createNativeQuery(anyString())).thenReturn(
            doanhThuQuery, tongDonQuery, tongSanPhamQuery, tongKhachHangQuery, doanhThuKyTruocQuery
        );

        LocalDateTime start = LocalDateTime.of(2026, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 20, 20, 0);

        ThongKeTongQuanDTO result = reportService.thongKeTongQuan(start, end);

        assertThat(result.tongDoanhThu()).isEqualTo(200L);
        assertThat(result.tongDonHang()).isEqualTo(5L);
        assertThat(result.tongSanPhamBan()).isEqualTo(12L);
        assertThat(result.tongKhachHang()).isEqualTo(3L);
        assertThat(result.tyLeTangTruong()).isEqualTo(100.0);

        verify(doanhThuKyTruocQuery).setParameter(
            eq("startDate"), eq(Timestamp.valueOf(LocalDateTime.of(2025, 12, 30, 0, 0)))
        );
        verify(doanhThuKyTruocQuery).setParameter(
            eq("endDate"), eq(Timestamp.valueOf(LocalDateTime.of(2026, 1, 9, 23, 59, 59)))
        );
    }

    @Test
    void thongKeTongQuanShouldKeepGrowthZeroWhenPreviousRevenueIsZero() {
        Query doanhThuQuery = mockSingleResultQuery(500L);
        Query tongDonQuery = mockSingleResultQuery(4L);
        Query tongSanPhamQuery = mockSingleResultQuery(9L);
        Query tongKhachHangQuery = mockSingleResultQuery(2L);
        Query doanhThuKyTruocQuery = mockSingleResultQuery(0L);

        when(em.createNativeQuery(anyString())).thenReturn(
            doanhThuQuery, tongDonQuery, tongSanPhamQuery, tongKhachHangQuery, doanhThuKyTruocQuery
        );

        ThongKeTongQuanDTO result = reportService.thongKeTongQuan(
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 2, 28, 23, 59, 59)
        );

        assertThat(result.tyLeTangTruong()).isEqualTo(0.0);
    }

    @Test
    void thongKeKhachHangVIPShouldMapRowsAndForceTop10Limit() {
        Query query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {1, "Nguyen Van A", "a@mail.com", BigDecimal.valueOf(2), BigDecimal.valueOf(1000), 500.0});
        rows.add(new Object[] {null, null, null, null, null, null});
        when(query.getResultList()).thenReturn(rows);
        when(em.createNativeQuery(anyString())).thenReturn(query);

        List<ThongKeKhachHangVIPDTO> result = reportService.thongKeKhachHangVIP(
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59, 59)
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).maTaiKhoan()).isEqualTo(1);
        assertThat(result.get(0).hoTen()).isEqualTo("Nguyen Van A");
        assertThat(result.get(0).tongDonHang()).isEqualTo(2L);
        assertThat(result.get(1).maTaiKhoan()).isEqualTo(0);
        assertThat(result.get(1).hoTen()).isEmpty();
        assertThat(result.get(1).tongChiTieu()).isEqualTo(0L);

        verify(query).setMaxResults(10);
    }

    @Test
    void thongKeDoanhThuTheoDanhMucShouldMapNullAsDefaultValues() {
        Query query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {"Anime", 1500L, 30L, 100L, 20L, 50L});
        rows.add(new Object[] {null, null, null, null, null, null});
        when(query.getResultList()).thenReturn(rows);
        when(em.createNativeQuery(anyString())).thenReturn(query);

        List<ThongKeDanhMucDTO> result = reportService.thongKeDoanhThuTheoDanhMuc(
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59, 59)
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).tenDanhMuc()).isEqualTo("Anime");
        assertThat(result.get(0).tongDoanhThu()).isEqualTo(1500L);
        assertThat(result.get(1).tenDanhMuc()).isEmpty();
        assertThat(result.get(1).tongDoanhThu()).isEqualTo(0L);
        assertThat(result.get(1).giaTrungBinh()).isEqualTo(0L);
    }

    @Test
    void thongKeSanPhamBanChayShouldRespectLimitAndMapNumericValues() {
        Query query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {"MH01", "RX-78", "Gundam", BigDecimal.valueOf(7), BigDecimal.valueOf(3500), BigDecimal.valueOf(500)});
        when(query.getResultList()).thenReturn(rows);
        when(em.createNativeQuery(anyString())).thenReturn(query);

        List<ThongKeSanPhamBanChayDTO> result = reportService.thongKeSanPhamBanChay(
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59, 59),
            5
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).maMoHinh()).isEqualTo("MH01");
        assertThat(result.get(0).tongSoLuong()).isEqualTo(7L);
        assertThat(result.get(0).tongDoanhThu()).isEqualTo(3500L);
        assertThat(result.get(0).giaTrungBinh()).isEqualTo(500.0);
        verify(query).setMaxResults(5);
    }

    @Test
    void thongKeDoanhThuTheoThangShouldPassThroughYearParameter() {
        Query query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {1, 1000L, 2L});
        when(query.getResultList()).thenReturn(rows);
        when(em.createNativeQuery(anyString())).thenReturn(query);

        List<Object[]> result = reportService.thongKeDoanhThuTheoThang(2026);

        assertThat(result).isEqualTo(rows);
        verify(query).setParameter("year", 2026);
    }

    private Query mockSingleResultQuery(Object result) {
        Query query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(result);
        return query;
    }
}
