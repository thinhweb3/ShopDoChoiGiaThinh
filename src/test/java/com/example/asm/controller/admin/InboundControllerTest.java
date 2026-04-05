package com.example.asm.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import com.example.asm.entity.DonNhap;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.service.AuthService;
import com.example.asm.service.InboundService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class InboundControllerTest {

    @Test
    void listShouldUseDateFilterWhenProvided() {
        InboundController controller = new InboundController();
        InboundService inboundService = mock(InboundService.class);
        BienTheMoHinhRepository bienTheRepo = mock(BienTheMoHinhRepository.class);
        ReflectionTestUtils.setField(controller, "inboundService", inboundService);
        ReflectionTestUtils.setField(controller, "bienTheRepo", bienTheRepo);

        DonNhap dn = DonNhap.builder().maDonNhap(1).build();
        when(inboundService.findByDateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))).thenReturn(List.of(dn));
        when(inboundService.findById(1)).thenReturn(dn);
        when(inboundService.findDetails(1)).thenReturn(List.of());
        when(bienTheRepo.findAll()).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.list(1, "2026-01-01", "2026-01-31", model);

        assertThat(view).isEqualTo("admin/inbound");
        assertThat(model.getAttribute("orders")).isEqualTo(List.of(dn));
        verify(inboundService).findByDateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
    }

    @Test
    void createShouldRedirectToLoginWhenNoUser() {
        InboundController controller = new InboundController();
        AuthService authService = mock(AuthService.class);
        ReflectionTestUtils.setField(controller, "authService", authService);
        when(authService.getUser()).thenReturn(null);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.create("ghi chu", ra);

        assertThat(view).isEqualTo("redirect:/auth/login");
        assertThat(ra.getFlashAttributes().get("error")).isEqualTo("Bạn cần đăng nhập để tạo đơn nhập");
    }

    @Test
    void createShouldCreateOrderAndRedirectToDetail() {
        InboundController controller = new InboundController();
        AuthService authService = mock(AuthService.class);
        InboundService inboundService = mock(InboundService.class);
        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "inboundService", inboundService);

        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(1).build();
        DonNhap dn = DonNhap.builder().maDonNhap(55).build();
        when(authService.getUser()).thenReturn(user);
        when(inboundService.create(user, "abc")).thenReturn(dn);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.create("abc", ra);

        assertThat(view).isEqualTo("redirect:/admin/inbound?don=55");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Đã tạo đơn nhập #55");
    }

    @Test
    void addDetailShouldDelegateAndRedirect() {
        InboundController controller = new InboundController();
        InboundService inboundService = mock(InboundService.class);
        ReflectionTestUtils.setField(controller, "inboundService", inboundService);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.addDetail(9, 3, 4, 500L, ra);

        assertThat(view).isEqualTo("redirect:/admin/inbound?don=9");
        verify(inboundService).addDetail(9, 3, 4, 500L);
    }

    @Test
    void deleteDetailShouldDelegateAndKeepParentOrderId() {
        InboundController controller = new InboundController();
        InboundService inboundService = mock(InboundService.class);
        ReflectionTestUtils.setField(controller, "inboundService", inboundService);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.deleteDetail(12, 99, ra);

        assertThat(view).isEqualTo("redirect:/admin/inbound?don=12");
        verify(inboundService).deleteDetail(99);
    }
}
