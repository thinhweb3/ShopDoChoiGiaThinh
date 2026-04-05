package com.example.asm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.ChiTietDonHang;
import com.example.asm.entity.DonHang;
import com.example.asm.entity.GioHang;
import com.example.asm.entity.KhuyenMai;
import com.example.asm.entity.MoHinh;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.ChiTietDonHangRepository;
import com.example.asm.repository.DonHangRepository;
import com.example.asm.repository.GioHangRepository;
import com.example.asm.repository.KhuyenMaiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrderServiceTest {

    private OrderService orderService;
    private DonHangRepository donHangRepo;
    private ChiTietDonHangRepository chiTietRepo;
    private GioHangRepository gioHangRepo;
    private BienTheMoHinhRepository bienTheRepo;
    private KhuyenMaiRepository kmRepo;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
        donHangRepo = mock(DonHangRepository.class);
        chiTietRepo = mock(ChiTietDonHangRepository.class);
        gioHangRepo = mock(GioHangRepository.class);
        bienTheRepo = mock(BienTheMoHinhRepository.class);
        kmRepo = mock(KhuyenMaiRepository.class);

        ReflectionTestUtils.setField(orderService, "donHangRepo", donHangRepo);
        ReflectionTestUtils.setField(orderService, "chiTietRepo", chiTietRepo);
        ReflectionTestUtils.setField(orderService, "gioHangRepo", gioHangRepo);
        ReflectionTestUtils.setField(orderService, "bienTheRepo", bienTheRepo);
        ReflectionTestUtils.setField(orderService, "kmRepo", kmRepo);
        ReflectionTestUtils.setField(orderService, "shippingFee", 30_000L);
    }

    @Test
    void placeOrderShouldThrowWhenCartIsEmpty() {
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(1).build();
        when(gioHangRepo.findByTaiKhoan_MaTaiKhoan(1)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.placeOrder(user, "HN", "", null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Giỏ hàng trống");
    }

    @Test
    void placeOrderShouldApplyVoucherAndCapDiscount() {
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(1).build();
        BienTheMoHinh bt = BienTheMoHinh.builder()
            .maBienThe(10)
            .giaBan(100_000L)
            .soLuongTon(10)
            .moHinh(MoHinh.builder().tenMoHinh("RX").build())
            .build();
        GioHang cart = GioHang.builder().taiKhoan(user).bienThe(bt).soLuong(2).build();

        KhuyenMai voucherParam = KhuyenMai.builder().maCode("KM10").build();
        KhuyenMai voucherDb = KhuyenMai.builder()
            .maCode("KM10")
            .trangThai(true)
            .soLuong(3)
            .phanTramGiam(20)
            .giamToiDa(30_000L)
            .donToiThieu(50_000L)
            .ngayBatDau(LocalDateTime.now().minusDays(1))
            .ngayKetThuc(LocalDateTime.now().plusDays(1))
            .build();

        when(gioHangRepo.findByTaiKhoan_MaTaiKhoan(1)).thenReturn(List.of(cart));
        when(kmRepo.findById("KM10")).thenReturn(Optional.of(voucherDb));
        when(donHangRepo.save(any(DonHang.class))).thenAnswer(inv -> {
            DonHang dh = inv.getArgument(0);
            dh.setMaDonHang(99);
            return dh;
        });
        when(chiTietRepo.save(any(ChiTietDonHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DonHang result = orderService.placeOrder(user, "HCM", "note", voucherParam);

        assertThat(result.getTienGiamGia()).isEqualTo(30_000L);
        assertThat(result.getTongTienHang()).isEqualTo(200_000L);
        assertThat(result.getTongTien()).isEqualTo(200_000L);
        assertThat(result.getKhuyenMai()).isEqualTo(voucherDb);
        assertThat(voucherDb.getSoLuong()).isEqualTo(2);
        verify(kmRepo).save(voucherDb);
        verify(gioHangRepo).deleteByTaiKhoan_MaTaiKhoan(1);
    }

    @Test
    void placeOrderShouldIgnoreInvalidVoucher() {
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(2).build();
        BienTheMoHinh bt = BienTheMoHinh.builder()
            .maBienThe(11)
            .giaBan(50_000L)
            .soLuongTon(5)
            .moHinh(MoHinh.builder().tenMoHinh("A").build())
            .build();
        GioHang cart = GioHang.builder().taiKhoan(user).bienThe(bt).soLuong(1).build();
        KhuyenMai voucherParam = KhuyenMai.builder().maCode("KMX").build();
        KhuyenMai expired = KhuyenMai.builder()
            .maCode("KMX")
            .trangThai(true)
            .soLuong(5)
            .soTienGiam(20_000L)
            .donToiThieu(10_000L)
            .ngayBatDau(LocalDateTime.now().minusDays(5))
            .ngayKetThuc(LocalDateTime.now().minusDays(1))
            .build();

        when(gioHangRepo.findByTaiKhoan_MaTaiKhoan(2)).thenReturn(List.of(cart));
        when(kmRepo.findById("KMX")).thenReturn(Optional.of(expired));
        when(donHangRepo.save(any(DonHang.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chiTietRepo.save(any(ChiTietDonHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DonHang result = orderService.placeOrder(user, "DN", "", voucherParam);

        assertThat(result.getTienGiamGia()).isEqualTo(0L);
        assertThat(result.getKhuyenMai()).isNull();
        verify(kmRepo, never()).save(any(KhuyenMai.class));
    }

    @Test
    void confirmPaymentShouldThrowWhenOrderNotOwnedByUser() {
        TaiKhoan owner = TaiKhoan.builder().maTaiKhoan(5).build();
        DonHang order = DonHang.builder().maDonHang(1).taiKhoan(owner).trangThaiThanhToan("Chờ thanh toán").build();
        when(donHangRepo.findById(1)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirmPayment(1, 99))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("không thuộc");
    }

    @Test
    void confirmPaymentShouldReturnWhenAlreadyPaid() {
        TaiKhoan owner = TaiKhoan.builder().maTaiKhoan(5).build();
        DonHang order = DonHang.builder().maDonHang(1).taiKhoan(owner).trangThaiThanhToan("Đã thanh toán").build();
        when(donHangRepo.findById(1)).thenReturn(Optional.of(order));

        orderService.confirmPayment(1, 5);

        verify(chiTietRepo, never()).findByDonHang_MaDonHang(anyInt());
        verify(donHangRepo, never()).save(any(DonHang.class));
    }

    @Test
    void confirmPaymentShouldUpdateStockAndPaymentStatus() {
        TaiKhoan owner = TaiKhoan.builder().maTaiKhoan(7).build();
        MoHinh moHinh = MoHinh.builder().tenMoHinh("B").build();
        BienTheMoHinh bt = BienTheMoHinh.builder().maBienThe(20).moHinh(moHinh).soLuongTon(10).build();
        DonHang order = DonHang.builder().maDonHang(2).taiKhoan(owner).trangThaiThanhToan("Chờ thanh toán").build();
        ChiTietDonHang ct = ChiTietDonHang.builder().bienThe(bt).soLuong(3).build();

        when(donHangRepo.findById(2)).thenReturn(Optional.of(order));
        when(chiTietRepo.findByDonHang_MaDonHang(2)).thenReturn(List.of(ct));

        orderService.confirmPayment(2, 7);

        assertThat(bt.getSoLuongTon()).isEqualTo(7);
        assertThat(order.getTrangThaiThanhToan()).isEqualTo("Đã thanh toán");
        verify(bienTheRepo).save(bt);
        verify(donHangRepo).save(order);
    }

    @Test
    void confirmPaymentShouldThrowWhenStockInsufficient() {
        TaiKhoan owner = TaiKhoan.builder().maTaiKhoan(8).build();
        MoHinh moHinh = MoHinh.builder().tenMoHinh("Out").build();
        BienTheMoHinh bt = BienTheMoHinh.builder().maBienThe(21).moHinh(moHinh).soLuongTon(1).build();
        DonHang order = DonHang.builder().maDonHang(3).taiKhoan(owner).trangThaiThanhToan("Chờ thanh toán").build();
        ChiTietDonHang ct = ChiTietDonHang.builder().bienThe(bt).soLuong(2).build();

        when(donHangRepo.findById(3)).thenReturn(Optional.of(order));
        when(chiTietRepo.findByDonHang_MaDonHang(3)).thenReturn(List.of(ct));

        assertThatThrownBy(() -> orderService.confirmPayment(3, 8))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Hết hàng");
        verify(donHangRepo, never()).save(any(DonHang.class));
    }
}
