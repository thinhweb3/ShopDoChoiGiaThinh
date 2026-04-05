package com.example.asm.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.asm.entity.DanhMuc;
import com.example.asm.entity.MoHinh;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.YeuThich;
import com.example.asm.repository.MoHinhRepository;
import com.example.asm.repository.TaiKhoanRepository;
import com.example.asm.repository.YeuThichRepository;
import com.example.asm.service.AccountService;
import com.example.asm.service.AuthService;
import com.example.asm.service.CartService;
import com.example.asm.service.ProductService;
import com.example.asm.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class ShopControllerAdditionalTest {

    @Test
    void cartViewShouldRedirectWhenNotLogin() {
        CartController controller = new CartController();
        AuthService auth = mock(AuthService.class);
        ReflectionTestUtils.setField(controller, "authService", auth);
        when(auth.isLogin()).thenReturn(false);

        assertThat(controller.view(new ExtendedModelMap())).isEqualTo("redirect:/auth/login");
    }

    @Test
    void cartAddShouldSwallowExceptionAndRedirect() {
        CartController controller = new CartController();
        AuthService auth = mock(AuthService.class);
        CartService cart = mock(CartService.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(1).build();
        ReflectionTestUtils.setField(controller, "authService", auth);
        ReflectionTestUtils.setField(controller, "cartService", cart);
        when(auth.isLogin()).thenReturn(true);
        when(auth.getUser()).thenReturn(user);
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(cart).add(user, 2, 1);

        assertThat(controller.add(2)).isEqualTo("redirect:/cart/view");
    }

    @Test
    void cartClearShouldCallServiceWhenLogin() {
        CartController controller = new CartController();
        AuthService auth = mock(AuthService.class);
        CartService cart = mock(CartService.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(3).build();
        ReflectionTestUtils.setField(controller, "authService", auth);
        ReflectionTestUtils.setField(controller, "cartService", cart);
        when(auth.isLogin()).thenReturn(true);
        when(auth.getUser()).thenReturn(user);

        String view = controller.clear();

        assertThat(view).isEqualTo("redirect:/cart/view");
        verify(cart).clear(user);
    }

    @Test
    void favoriteToggleShouldRedirectLoginWhenUnauthenticated() {
        FavoriteController controller = new FavoriteController();
        AuthService auth = mock(AuthService.class);
        ReflectionTestUtils.setField(controller, "authService", auth);
        when(auth.isLogin()).thenReturn(false);

        String view = controller.toggle("MH1", "/x", new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("redirect:/auth/login");
    }

    @Test
    void favoriteToggleShouldDeleteExistingFavorite() {
        FavoriteController controller = new FavoriteController();
        AuthService auth = mock(AuthService.class);
        YeuThichRepository favRepo = mock(YeuThichRepository.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(1).build();
        YeuThich fav = YeuThich.builder().maYeuThich(10).build();
        ReflectionTestUtils.setField(controller, "authService", auth);
        ReflectionTestUtils.setField(controller, "yeuThichRepo", favRepo);
        when(auth.isLogin()).thenReturn(true);
        when(auth.getUser()).thenReturn(user);
        when(favRepo.findByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(1, "MH1")).thenReturn(Optional.of(fav));
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.toggle("MH1", "/product/detail/MH1", ra);

        assertThat(view).isEqualTo("redirect:/product/detail/MH1");
        assertThat(ra.getFlashAttributes().get("type")).isEqualTo("warning");
        verify(favRepo).delete(fav);
    }

    @Test
    void favoriteToggleShouldCreateNewFavoriteAndFallbackReferer() {
        FavoriteController controller = new FavoriteController();
        AuthService auth = mock(AuthService.class);
        YeuThichRepository favRepo = mock(YeuThichRepository.class);
        MoHinhRepository moHinhRepo = mock(MoHinhRepository.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(2).build();
        MoHinh mh = MoHinh.builder().maMoHinh("MH2").build();
        ReflectionTestUtils.setField(controller, "authService", auth);
        ReflectionTestUtils.setField(controller, "yeuThichRepo", favRepo);
        ReflectionTestUtils.setField(controller, "moHinhRepo", moHinhRepo);
        when(auth.isLogin()).thenReturn(true);
        when(auth.getUser()).thenReturn(user);
        when(favRepo.findByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(2, "MH2")).thenReturn(Optional.empty());
        when(moHinhRepo.findById("MH2")).thenReturn(Optional.of(mh));
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.toggle("MH2", null, ra);

        assertThat(view).isEqualTo("redirect:/home/index");
        assertThat(ra.getFlashAttributes().get("type")).isEqualTo("success");
        verify(favRepo).save(any(YeuThich.class));
    }

    @Test
    void favoriteRemoveShouldDeleteWhenFound() {
        FavoriteController controller = new FavoriteController();
        AuthService auth = mock(AuthService.class);
        YeuThichRepository favRepo = mock(YeuThichRepository.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(4).build();
        YeuThich fav = YeuThich.builder().maYeuThich(2).build();
        ReflectionTestUtils.setField(controller, "authService", auth);
        ReflectionTestUtils.setField(controller, "yeuThichRepo", favRepo);
        when(auth.isLogin()).thenReturn(true);
        when(auth.getUser()).thenReturn(user);
        when(favRepo.findByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(4, "MH4")).thenReturn(Optional.of(fav));
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.remove("MH4", ra);

        assertThat(view).isEqualTo("redirect:/favorite/view");
        verify(favRepo).delete(fav);
    }

    @Test
    void productSearchShouldEncodeKeyword() {
        ProductController controller = new ProductController();
        String view = controller.search("gundam hg");
        assertThat(view).isEqualTo("redirect:/product/list?keyword=gundam+hg");
    }

    @Test
    void productDetailShouldPopulateVisibleRelatedProducts() {
        ProductController controller = new ProductController();
        ProductService productService = mock(ProductService.class);
        DanhMuc dm = DanhMuc.builder().maDanhMuc(1).build();
        MoHinh item = MoHinh.builder().maMoHinh("M1").danhMuc(dm).build();
        MoHinh related = MoHinh.builder().maMoHinh("M2").danhMuc(dm).build();
        ReflectionTestUtils.setField(controller, "productService", productService);
        when(productService.findById("M1")).thenReturn(item);
        when(productService.findByCategoryId(1)).thenReturn(List.of(item, related));

        Model model = new ExtendedModelMap();
        String view = controller.detail("M1", model);

        assertThat(view).isEqualTo("product/detail");
        assertThat(model.getAttribute("relatedProducts")).isEqualTo(List.of(related));
    }

    @Test
    void ratingAddShouldHandleServiceError() {
        RatingController controller = new RatingController();
        AuthService auth = mock(AuthService.class);
        RatingService ratingService = mock(RatingService.class);
        TaiKhoan user = TaiKhoan.builder().maTaiKhoan(9).build();
        ReflectionTestUtils.setField(controller, "authService", auth);
        ReflectionTestUtils.setField(controller, "ratingService", ratingService);
        when(auth.isLogin()).thenReturn(true);
        when(auth.getUser()).thenReturn(user);
        org.mockito.Mockito.doThrow(new RuntimeException("db")).when(ratingService).addRating(user, "M9", (byte) 5, "ok");
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.addRating("M9", (byte) 5, "ok", ra);

        assertThat(view).isEqualTo("redirect:/product/detail/M9");
        assertThat(ra.getFlashAttributes().get("error")).toString().contains("Lỗi khi gửi đánh giá");
    }

    @Test
    void managerAccountViewShouldReadFromRepository() {
        ManagerAccountController controller = new ManagerAccountController();
        TaiKhoanRepository tkRepo = mock(TaiKhoanRepository.class);
        AccountService accountService = mock(AccountService.class);
        ReflectionTestUtils.setField(controller, "taiKhoanRepo", tkRepo);
        ReflectionTestUtils.setField(controller, "accountService", accountService);
        when(tkRepo.findAll()).thenReturn(List.of(TaiKhoan.builder().maTaiKhoan(1).build()));

        Model model = new ExtendedModelMap();
        String view = controller.view(model);

        assertThat(view).isEqualTo("fragments/managerAccount");
        assertThat(((List<?>) model.getAttribute("accounts"))).hasSize(1);
    }
}
