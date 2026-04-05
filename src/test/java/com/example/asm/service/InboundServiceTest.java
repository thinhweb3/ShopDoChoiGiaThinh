package com.example.asm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.ChiTietNhap;
import com.example.asm.entity.DonNhap;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.ChiTietNhapRepository;
import com.example.asm.repository.DonNhapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

class InboundServiceTest {

    private InboundService inboundService;
    private DonNhapRepository donNhapRepo;
    private ChiTietNhapRepository chiTietRepo;
    private BienTheMoHinhRepository bienTheRepo;

    @BeforeEach
    void setUp() {
        inboundService = new InboundService();
        donNhapRepo = mock(DonNhapRepository.class);
        chiTietRepo = mock(ChiTietNhapRepository.class);
        bienTheRepo = mock(BienTheMoHinhRepository.class);

        ReflectionTestUtils.setField(inboundService, "donNhapRepo", donNhapRepo);
        ReflectionTestUtils.setField(inboundService, "chiTietRepo", chiTietRepo);
        ReflectionTestUtils.setField(inboundService, "bienTheRepo", bienTheRepo);
    }

    @Test
    void findByDateRangeShouldUseDefaultStartWhenFromIsNull() {
        when(donNhapRepo.findByNgayNhapBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
            .thenReturn(List.of());

        inboundService.findByDateRange(null, LocalDate.of(2026, 2, 24));

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(donNhapRepo).findByNgayNhapBetween(fromCaptor.capture(), toCaptor.capture(), any(Sort.class));

        assertThat(fromCaptor.getValue()).isEqualTo(LocalDateTime.of(2000, 1, 1, 0, 0));
        assertThat(toCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 2, 24, 23, 59, 59, 999_999_999));
    }

    @Test
    void addDetailShouldIncreaseStockAndRefreshOrderTotal() {
        DonNhap dn = DonNhap.builder().maDonNhap(10).tongTienNhap(0L).build();
        BienTheMoHinh bt = BienTheMoHinh.builder().maBienThe(2).soLuongTon(5).build();
        ChiTietNhap saved = ChiTietNhap.builder().maChiTietNhap(20).donNhap(dn).bienThe(bt).soLuongNhap(3).giaNhap(100L).build();

        when(donNhapRepo.findById(10)).thenReturn(Optional.of(dn));
        when(bienTheRepo.findById(2)).thenReturn(Optional.of(bt));
        when(chiTietRepo.save(any(ChiTietNhap.class))).thenReturn(saved);
        when(chiTietRepo.findByDonNhap_MaDonNhap(10)).thenReturn(List.of(saved));
        when(donNhapRepo.save(any(DonNhap.class))).thenAnswer(inv -> inv.getArgument(0));

        ChiTietNhap result = inboundService.addDetail(10, 2, 3, 100L);

        assertThat(result.getMaChiTietNhap()).isEqualTo(20);
        assertThat(bt.getSoLuongTon()).isEqualTo(8);
        assertThat(dn.getTongTienNhap()).isEqualTo(300L);
        verify(bienTheRepo).save(bt);
        verify(donNhapRepo).save(dn);
    }

    @Test
    void updateDetailShouldThrowWhenResultingStockIsNegative() {
        BienTheMoHinh bt = BienTheMoHinh.builder().maBienThe(3).soLuongTon(1).build();
        DonNhap dn = DonNhap.builder().maDonNhap(11).build();
        ChiTietNhap ct = ChiTietNhap.builder().maChiTietNhap(30).donNhap(dn).bienThe(bt).soLuongNhap(5).giaNhap(200L).build();
        when(chiTietRepo.findById(30)).thenReturn(Optional.of(ct));

        assertThatThrownBy(() -> inboundService.updateDetail(30, 1, 200L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Tồn kho âm");
        verify(chiTietRepo, never()).save(any(ChiTietNhap.class));
    }

    @Test
    void deleteDetailShouldIgnoreMissingDetail() {
        when(chiTietRepo.findById(99)).thenReturn(Optional.empty());

        inboundService.deleteDetail(99);

        verify(bienTheRepo, never()).save(any(BienTheMoHinh.class));
    }

    @Test
    void deleteOrderShouldRollbackStockAndDeleteDetails() {
        DonNhap dn = DonNhap.builder().maDonNhap(15).build();
        BienTheMoHinh bt = BienTheMoHinh.builder().maBienThe(4).soLuongTon(2).build();
        ChiTietNhap ct = ChiTietNhap.builder().maChiTietNhap(40).donNhap(dn).bienThe(bt).soLuongNhap(5).giaNhap(100L).build();

        when(donNhapRepo.findById(15)).thenReturn(Optional.of(dn));
        when(chiTietRepo.findByDonNhap_MaDonNhap(15)).thenReturn(List.of(ct));

        inboundService.deleteOrder(15);

        assertThat(bt.getSoLuongTon()).isEqualTo(0);
        verify(bienTheRepo).save(bt);
        verify(chiTietRepo).deleteAll(List.of(ct));
        verify(donNhapRepo).delete(dn);
    }

    @Test
    void findByDateRangeShouldRespectProvidedBoundaries() {
        when(donNhapRepo.findByNgayNhapBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
            .thenReturn(List.of());
        LocalDate from = LocalDate.of(2026, 1, 5);
        LocalDate to = LocalDate.of(2026, 1, 10);

        inboundService.findByDateRange(from, to);

        verify(donNhapRepo).findByNgayNhapBetween(
            eq(from.atStartOfDay()),
            eq(to.plusDays(1).atStartOfDay().minusNanos(1)),
            any(Sort.class)
        );
    }
}
