package com.example.asm.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.asm.config.GoogleOauth2SupportConfig.GoogleOauth2AccountService;
import com.example.asm.entity.DonHang;
import com.example.asm.entity.KhuyenMai;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import com.example.asm.repository.KhuyenMaiRepository;
import com.example.asm.security.TaiKhoanPrincipal;
import com.example.asm.service.AccountService;
import com.example.asm.service.AuthService;
import com.example.asm.service.CartService;
import com.example.asm.service.OrderDetailService;
import com.example.asm.service.OrderService;
import com.example.asm.service.PaymentInboxService;
import com.example.asm.service.VaiTroService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class UserFlowControllerTest {

    @Test
    void authLoginShouldShowErrorWhenCredentialsInvalid() {
        AuthController controller = new AuthController();
        AuthService authService = mock(AuthService.class);
        HttpSession session = mock(HttpSession.class);
        ReflectionTestUtils.setField(controller, "authService", authService);
        when(authService.login("u", "p")).thenReturn(null);

        Model model = new ExtendedModelMap();
        String view = controller.login(model, "u", "p", session);

        assertThat(view).isEqualTo("fragments/login");
        assertThat(model.getAttribute("message")).isEqualTo("Sai tài khoản hoặc mật khẩu!");
    }

    @Test
    void authLoginShouldRedirectToSavedUrlWhenExists() {
        AuthController controller = new AuthController();
        AuthService authService = mock(AuthService.class);
        HttpSession session = mock(HttpSession.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(1).build();
        ReflectionTestUtils.setField(controller, "authService", authService);
        when(authService.login("u", "p")).thenReturn(user);
        when(session.getAttribute("security-uri")).thenReturn("/order/checkout");

        String view = controller.login(new ExtendedModelMap(), "u", "p", session);

        assertThat(view).isEqualTo("redirect:/order/checkout");
        verify(session).removeAttribute("security-uri");
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterController controller = new RegisterController();
        AccountService accountService = mock(AccountService.class);
        ReflectionTestUtils.setField(controller, "accountService", accountService);
        when(accountService.findByUsername("dup")).thenReturn(TaiKhoan.builder().maTaiKhoan(2).build());

        Model model = new ExtendedModelMap();
        String view = controller.register(model, "dup", "A", "a@mail.com", "123", "123");

        assertThat(view).isEqualTo("fragments/register");
        assertThat(model.getAttribute("message")).isEqualTo("Tên đăng nhập đã tồn tại!");
        verify(accountService, never()).save(any(TaiKhoan.class));
    }

    @Test
    void registerShouldCreateAccountWhenInputValid() {
        RegisterController controller = new RegisterController();
        AccountService accountService = mock(AccountService.class);
        VaiTroService vaiTroService = mock(VaiTroService.class);
        ReflectionTestUtils.setField(controller, "accountService", accountService);
        ReflectionTestUtils.setField(controller, "vaiTroService", vaiTroService);
        when(accountService.findByUsername("newu")).thenReturn(null);
        when(accountService.findByEmail("new@mail.com")).thenReturn(null);
        when(vaiTroService.getRequiredUserRole()).thenReturn(VaiTro.builder()
                .maVaiTro(3)
                .code(TaiKhoan.ROLE_USER)
                .tenHienThi("User")
                .moTa("Khách hàng")
                .choPhepTruyCapAdmin(false)
                .laVaiTroHeThong(true)
                .build());
        when(accountService.save(any(TaiKhoan.class))).thenAnswer(inv -> inv.getArgument(0));

        String view = controller.register(new ExtendedModelMap(), "newu", "New User", "new@mail.com", "123", "123");

        assertThat(view).isEqualTo("redirect:/auth/login?success=true");
    }

    @Test
    void profileShouldRedirectToLoginWhenNotAuthenticated() {
        ProfileController controller = new ProfileController();
        AuthService authService = mock(AuthService.class);
        ReflectionTestUtils.setField(controller, "authService", authService);
        when(authService.isLogin()).thenReturn(false);

        String view = controller.profile(new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/auth/login");
    }

    @Test
    void profileChangePasswordShouldRejectWrongCurrentPassword() {
        ProfileController controller = new ProfileController();
        AuthService authService = mock(AuthService.class);
        AccountService accountService = mock(AccountService.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(1).build();
        TaiKhoan dbUser = TaiKhoan.builder().maTaiKhoan(1).matKhau("old").build();
        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "accountService", accountService);
        when(authService.getUser()).thenReturn(user);
        when(accountService.findById(1)).thenReturn(dbUser);

        Model model = new ExtendedModelMap();
        String view = controller.changePassword(model, "wrong", "new", "new");

        assertThat(view).isEqualTo("fragments/profile");
        assertThat(model.getAttribute("messPass")).isEqualTo("Mật khẩu hiện tại không đúng!");
    }

    @Test
    void orderApplyVoucherShouldRejectInactiveVoucher() {
        OrderController controller = new OrderController();
        AuthService authService = mock(AuthService.class);
        CartService cartService = mock(CartService.class);
        KhuyenMaiRepository kmRepo = mock(KhuyenMaiRepository.class);
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(1).build();

        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "cartService", cartService);
        ReflectionTestUtils.setField(controller, "shippingFee", 30_000L);
        ReflectionTestUtils.setField(controller, "kmRepo", kmRepo);
        when(authService.isLogin()).thenReturn(true);
        when(authService.getUser()).thenReturn(user);
        when(cartService.getAmount(user)).thenReturn(500_000L);
        when(kmRepo.findById("NOPE")).thenReturn(Optional.empty());

        String view = controller.applyVoucher("NOPE", session, ra);

        assertThat(view).isEqualTo("redirect:/order/checkout");
        assertThat(ra.getFlashAttributes().get("voucherError")).isEqualTo("Mã không tồn tại hoặc đã bị khóa!");
        verify(session).removeAttribute("voucher");
    }

    @Test
    void orderCheckoutShouldCalculateDiscountByPercentWithCap() {
        OrderController controller = new OrderController();
        AuthService authService = mock(AuthService.class);
        CartService cartService = mock(CartService.class);
        HttpSession session = mock(HttpSession.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(5).build();
        KhuyenMai km = KhuyenMai.builder().phanTramGiam(30).giamToiDa(20_000L).build();

        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "cartService", cartService);
        ReflectionTestUtils.setField(controller, "shippingFee", 30_000L);
        when(authService.isLogin()).thenReturn(true);
        when(authService.getUser()).thenReturn(user);
        when(cartService.getCount(user)).thenReturn(1);
        when(cartService.getAmount(user)).thenReturn(100_000L);
        when(cartService.getCart(user)).thenReturn(java.util.List.of());
        when(session.getAttribute("voucher")).thenReturn(km);

        Model model = new ExtendedModelMap();
        String view = controller.checkout(model, session);

        assertThat(view).isEqualTo("fragments/checkout");
        assertThat(model.getAttribute("originalAmount")).isEqualTo(100_000L);
        assertThat(model.getAttribute("discountAmount")).isEqualTo(20_000L);
        assertThat(model.getAttribute("amount")).isEqualTo(80_000L);
        assertThat(model.getAttribute("shippingFee")).isEqualTo(30_000L);
        assertThat(model.getAttribute("payableAmount")).isEqualTo(110_000L);
    }

    @Test
    void orderPurchaseShouldRejectAddressWithSpecialChars() {
        OrderController controller = new OrderController();
        AuthService authService = mock(AuthService.class);
        ReflectionTestUtils.setField(controller, "authService", authService);
        when(authService.isLogin()).thenReturn(true);

        RedirectAttributes ra = new RedirectAttributesModelMap();
        String view = controller.purchase("so 1 + ngo", "", "COD", mock(HttpSession.class), ra);

        assertThat(view).isEqualTo("redirect:/order/checkout");
        assertThat(ra.getFlashAttributes().get("message")).isEqualTo("Địa chỉ nhận hàng không được chứa ký tự đặc biệt");
    }

    @Test
    void orderPurchaseShouldRedirectToQrWhenTransferPayment() {
        OrderController controller = new OrderController();
        AuthService authService = mock(AuthService.class);
        OrderService orderService = mock(OrderService.class);
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(10).build();
        DonHang order = DonHang.builder().maDonHang(88).build();

        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        when(authService.isLogin()).thenReturn(true);
        when(authService.getUser()).thenReturn(user);
        when(session.getAttribute("voucher")).thenReturn(null);
        when(orderService.placeOrder(eq(user), anyString(), anyString(), any())).thenReturn(order);

        String view = controller.purchase("123 street", "note", "CHUYEN_KHOAN", session, ra);

        assertThat(view).isEqualTo("redirect:/order/payment-qr/88");
        verify(session).removeAttribute("voucher");
    }

    @Test
    void orderDetailShouldRejectAccessToAnotherUserOrder() {
        OrderController controller = new OrderController();
        AuthService authService = mock(AuthService.class);
        OrderService orderService = mock(OrderService.class);
        OrderDetailService orderDetailService = mock(OrderDetailService.class);

        TaiKhoan loginUser = TaiKhoan.builder().maTaiKhoan(1).build();
        TaiKhoan owner = TaiKhoan.builder().maTaiKhoan(2).build();
        DonHang order = DonHang.builder().maDonHang(10).taiKhoan(owner).build();

        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        ReflectionTestUtils.setField(controller, "orderDetailService", orderDetailService);
        when(authService.isLogin()).thenReturn(true);
        when(authService.getUser()).thenReturn(loginUser);
        when(orderService.findById(10)).thenReturn(order);

        String view = controller.detail(10, new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/order/list");
    }

    @Test
    void orderConfirmPaymentShouldUpdateOrderWhenGmailMatchExists() {
        OrderController controller = new OrderController();
        AuthService authService = mock(AuthService.class);
        OrderService orderService = mock(OrderService.class);
        PaymentInboxService paymentInboxService = mock(PaymentInboxService.class);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(10).build();
        DonHang order = DonHang.builder()
                .maDonHang(88)
                .taiKhoan(user)
                .tongTien(230_000L)
                .trangThaiThanhToan("Chờ thanh toán")
                .build();

        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        ReflectionTestUtils.setField(controller, "paymentInboxService", paymentInboxService);
        when(authService.isLogin()).thenReturn(true);
        when(authService.getUser()).thenReturn(user);
        when(orderService.findById(88)).thenReturn(order);
        when(paymentInboxService.findMatchingPaymentEmail(order))
                .thenReturn(Optional.of(new PaymentInboxService.PaymentEmailMatch(
                        "Bao co DH88",
                        "bank@example.com",
                        LocalDateTime.of(2026, 3, 29, 12, 0))));

        String view = controller.confirmPayment(88, ra);

        assertThat(view).isEqualTo("redirect:/order/success");
        verify(orderService).confirmPayment(88, 10);
        assertThat(ra.getFlashAttributes().get("paymentSuccess").toString())
                .contains("Thanh toán chuyển khoản đã được xác nhận");
    }

    @Test
    void orderConfirmPaymentShouldReturnToQrWhenGmailMatchMissing() {
        OrderController controller = new OrderController();
        AuthService authService = mock(AuthService.class);
        OrderService orderService = mock(OrderService.class);
        PaymentInboxService paymentInboxService = mock(PaymentInboxService.class);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(10).build();
        DonHang order = DonHang.builder()
                .maDonHang(89)
                .taiKhoan(user)
                .tongTien(330_000L)
                .trangThaiThanhToan("Chờ thanh toán")
                .build();

        ReflectionTestUtils.setField(controller, "authService", authService);
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        ReflectionTestUtils.setField(controller, "paymentInboxService", paymentInboxService);
        when(authService.isLogin()).thenReturn(true);
        when(authService.getUser()).thenReturn(user);
        when(orderService.findById(89)).thenReturn(order);
        when(paymentInboxService.findMatchingPaymentEmail(order)).thenReturn(Optional.empty());

        String view = controller.confirmPayment(89, ra);

        assertThat(view).isEqualTo("redirect:/order/payment-qr/89");
        assertThat(ra.getFlashAttributes().get("message").toString()).contains("Chưa tìm thấy email báo có");
        verify(orderService, never()).confirmPayment(anyInt(), anyInt());
    }

    @Test
    void googleLoginShouldMapExistingAccountByEmailIgnoringCase() {
        var taiKhoanRepo = mock(com.example.asm.repository.TaiKhoanRepository.class);
        AccountService accountService = mock(AccountService.class);
        VaiTroService vaiTroService = mock(VaiTroService.class);
        GoogleOauth2AccountService service = new GoogleOauth2AccountService(taiKhoanRepo, accountService, vaiTroService);
        TaiKhoan existing = TaiKhoan.builder()
                .maTaiKhoan(1)
                .tenDangNhap("existing-user")
                .email("user@gmail.com")
                .trangThai(true)
                .build();

        when(taiKhoanRepo.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.of(existing));

        TaiKhoan result = service.resolveUser("User@gmail.com", "Google User");

        assertThat(result).isSameAs(existing);
        verify(accountService, never()).save(any(TaiKhoan.class));
    }

    @Test
    void googleLoginShouldCreateLocalAccountWhenEmailNotFound() {
        var taiKhoanRepo = mock(com.example.asm.repository.TaiKhoanRepository.class);
        AccountService accountService = mock(AccountService.class);
        VaiTroService vaiTroService = mock(VaiTroService.class);
        GoogleOauth2AccountService service = new GoogleOauth2AccountService(taiKhoanRepo, accountService, vaiTroService);
        when(vaiTroService.getRequiredUserRole()).thenReturn(VaiTro.builder()
                .maVaiTro(3)
                .code(TaiKhoan.ROLE_USER)
                .tenHienThi("User")
                .moTa("Khách hàng")
                .choPhepTruyCapAdmin(false)
                .laVaiTroHeThong(true)
                .build());

        when(taiKhoanRepo.findByEmailIgnoreCase("new.user@gmail.com")).thenReturn(Optional.empty());
        when(taiKhoanRepo.existsByTenDangNhap("new.user")).thenReturn(false);
        when(accountService.encodePassword(anyString())).thenReturn("encoded-random");
        when(accountService.save(any(TaiKhoan.class))).thenAnswer(inv -> inv.getArgument(0));

        TaiKhoan result = service.resolveUser("new.user@gmail.com", "New User");

        assertThat(result.getTenDangNhap()).isEqualTo("new.user");
        assertThat(result.getEmail()).isEqualTo("new.user@gmail.com");
        assertThat(result.getHoTen()).isEqualTo("New User");
        assertThat(result.getMatKhau()).isEqualTo("encoded-random");
        assertThat(result.getTrangThai()).isTrue();
    }

    @Test
    void googleLoginShouldRejectLockedLocalAccount() {
        var taiKhoanRepo = mock(com.example.asm.repository.TaiKhoanRepository.class);
        AccountService accountService = mock(AccountService.class);
        VaiTroService vaiTroService = mock(VaiTroService.class);
        GoogleOauth2AccountService service = new GoogleOauth2AccountService(taiKhoanRepo, accountService, vaiTroService);
        TaiKhoan locked = TaiKhoan.builder()
                .email("locked@gmail.com")
                .trangThai(false)
                .build();

        when(taiKhoanRepo.findByEmailIgnoreCase("locked@gmail.com")).thenReturn(Optional.of(locked));

        assertThatThrownBy(() -> service.resolveUser("locked@gmail.com", "Locked User"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bị khóa");
    }

    @Test
    void authServiceShouldResolveTaiKhoanPrincipalFromSecurityContext() {
        AuthService authService = new AuthService();
        HttpSession session = mock(HttpSession.class);
        var taiKhoanRepo = mock(com.example.asm.repository.TaiKhoanRepository.class);
        ReflectionTestUtils.setField(authService, "session", session);
        ReflectionTestUtils.setField(authService, "taiKhoanRepo", taiKhoanRepo);

        TaiKhoan principalUser = TaiKhoan.builder()
                .maTaiKhoan(7)
                .tenDangNhap("google.user")
                .email("google.user@gmail.com")
                .trangThai(true)
                .build();
        TaiKhoan persistedUser = TaiKhoan.builder()
                .maTaiKhoan(7)
                .tenDangNhap("google.user")
                .email("google.user@gmail.com")
                .trangThai(true)
                .build();

        when(taiKhoanRepo.findById(7)).thenReturn(Optional.of(persistedUser));

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                new TestTaiKhoanPrincipal(principalUser),
                null,
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            TaiKhoan resolved = authService.getUser();

            assertThat(resolved).isSameAs(persistedUser);
            verify(session).setAttribute("user", persistedUser);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static final class TestTaiKhoanPrincipal implements TaiKhoanPrincipal {

        private final TaiKhoan taiKhoan;

        private TestTaiKhoanPrincipal(TaiKhoan taiKhoan) {
            this.taiKhoan = taiKhoan;
        }

        @Override
        public TaiKhoan getTaiKhoan() {
            return taiKhoan;
        }
    }
}
