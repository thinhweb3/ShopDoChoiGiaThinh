package com.example.asm.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.ChiTietDonHang;
import com.example.asm.entity.DonHang;
import com.example.asm.entity.MoHinh;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.ChiTietDonHangRepository;
import com.example.asm.repository.DonHangRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class AdminOrderControllerTest {

    @Test
    void indexShouldUseKeywordAndStatusSearchBranch() {
        AdminOrderController controller = new AdminOrderController();
        DonHangRepository donHangRepo = mock(DonHangRepository.class);
        ChiTietDonHangRepository chiTietRepo = mock(ChiTietDonHangRepository.class);
        ReflectionTestUtils.setField(controller, "donHangRepo", donHangRepo);
        ReflectionTestUtils.setField(controller, "chiTietRepo", chiTietRepo);

        Page<DonHang> page = new PageImpl<>(List.of(DonHang.builder().maDonHang(1).build()));
        when(donHangRepo.searchByKeywordAndStatuses(eq("abc"), eq(List.of("Đang giao", "Đang giao hàng")), any(Pageable.class))).thenReturn(page);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.index(model, 0, 10, "abc", "Đang giao", "ngayDat", "desc");

        assertThat(view).isEqualTo("admin/admin-order");
        assertThat(model.getAttribute("orders")).isEqualTo(page.getContent());
        verify(donHangRepo).searchByKeywordAndStatuses(eq("abc"), eq(List.of("Đang giao", "Đang giao hàng")), any(Pageable.class));
    }

    @Test
    void getOrderApiShouldReturnNotFoundWhenOrderMissing() {
        AdminOrderController controller = new AdminOrderController();
        DonHangRepository donHangRepo = mock(DonHangRepository.class);
        ReflectionTestUtils.setField(controller, "donHangRepo", donHangRepo);
        when(donHangRepo.findById(99)).thenReturn(Optional.empty());

        var response = controller.getOrderApi(99);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getOrderApiShouldReturnOrderAndProductsWhenFound() {
        AdminOrderController controller = new AdminOrderController();
        DonHangRepository donHangRepo = mock(DonHangRepository.class);
        ChiTietDonHangRepository chiTietRepo = mock(ChiTietDonHangRepository.class);
        ReflectionTestUtils.setField(controller, "donHangRepo", donHangRepo);
        ReflectionTestUtils.setField(controller, "chiTietRepo", chiTietRepo);

        TaiKhoan tk = TaiKhoan.builder().hoTen("A").soDienThoai("0123").build();
        MoHinh mh = MoHinh.builder().tenMoHinh("RX").hinhAnh("a.jpg").build();
        BienTheMoHinh bt = BienTheMoHinh.builder().moHinh(mh).kichThuoc("M").build();
        DonHang dh = DonHang.builder()
            .maDonHang(1)
            .taiKhoan(tk)
            .diaChiGiaoHang("HN")
            .tongTien(100L)
            .trangThai("Chờ xử lý")
            .trangThaiThanhToan("Chờ thanh toán")
            .ngayDat(java.time.LocalDateTime.now())
            .phiVanChuyen(30L)
            .tienGiamGia(0L)
            .build();
        ChiTietDonHang ct = ChiTietDonHang.builder().bienThe(bt).soLuong(2).donGia(50L).build();

        when(donHangRepo.findById(1)).thenReturn(Optional.of(dh));
        when(chiTietRepo.findByDonHang_MaDonHang(1)).thenReturn(List.of(ct));

        var response = controller.getOrderApi(1);
        Map<String, Object> body = response.getBody();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(body.get("maDonHang")).isEqualTo(1);
        assertThat(body.get("trangThai")).isEqualTo("Đặt hàng");
        assertThat(body.get("trangThaiThanhToan")).isEqualTo("Chưa nhận tiền");
        assertThat(((List<?>) body.get("products"))).hasSize(1);
    }

    @Test
    void updateStatusShouldHandleExceptionAndSetErrorMessage() {
        AdminOrderController controller = new AdminOrderController();
        DonHangRepository donHangRepo = mock(DonHangRepository.class);
        ReflectionTestUtils.setField(controller, "donHangRepo", donHangRepo);
        when(donHangRepo.findById(10)).thenThrow(new RuntimeException("db fail"));
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.updateStatus(10, "Đã giao", "Đã nhận tiền", ra);

        assertThat(view).isEqualTo("redirect:/admin/orders");
        assertThat(ra.getFlashAttributes().get("messageType")).isEqualTo("error");
    }

    @Test
    void getOrderStatsShouldReturnAllCounters() {
        AdminOrderController controller = new AdminOrderController();
        DonHangRepository donHangRepo = mock(DonHangRepository.class);
        ReflectionTestUtils.setField(controller, "donHangRepo", donHangRepo);
        when(donHangRepo.countByTrangThaiIn(List.of("Đặt hàng", "Chờ xử lý", "Đã xác nhận"))).thenReturn(3L);
        when(donHangRepo.countByTrangThaiIn(List.of("Đang giao", "Đang giao hàng"))).thenReturn(3L);
        when(donHangRepo.countByTrangThaiIn(List.of("Đã giao", "Hoàn thành"))).thenReturn(4L);
        when(donHangRepo.countByTrangThai("Đã hủy")).thenReturn(5L);
        when(donHangRepo.count()).thenReturn(15L);

        var response = controller.getOrderStats();
        Map<String, Long> body = response.getBody();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(body).isEqualTo(new HashMap<>(Map.of(
            "datHang", 3L,
            "dangGiao", 3L,
            "daGiao", 4L,
            "daHuy", 5L,
            "tongDon", 15L
        )));
    }
}
