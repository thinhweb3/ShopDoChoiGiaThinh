package com.example.asm.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.asm.dto.AdminRecentOrderItemDTO;
import com.example.asm.dto.ThongKeTongQuanDTO;
import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.DanhMuc;
import com.example.asm.entity.HangSanXuat;
import com.example.asm.entity.KhuyenMai;
import com.example.asm.entity.MoHinh;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.DanhMucRepository;
import com.example.asm.repository.HangSanXuatRepository;
import com.example.asm.repository.KhuyenMaiRepository;
import com.example.asm.repository.LoaiHangRepository;
import com.example.asm.repository.MoHinhRepository;
import com.example.asm.repository.TaiKhoanRepository;
import com.example.asm.repository.VaiTroRepository;
import com.example.asm.service.AdminDashboardService;
import com.example.asm.service.AuthService;
import com.example.asm.service.ReportService;
import com.example.asm.service.VaiTroService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class AdminCrudControllersTest {

    @Test
    void categorySaveShouldRejectDuplicateNameWhenCreate() {
        AdminCategoryController c = new AdminCategoryController();
        DanhMucRepository repo = mock(DanhMucRepository.class);
        ReflectionTestUtils.setField(c, "danhMucRepo", repo);
        when(repo.existsByTenDanhMuc("Figure")).thenReturn(true);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = c.save(DanhMuc.builder().tenDanhMuc("Figure").build(), ra);

        assertThat(view).isEqualTo("redirect:/admin/categories");
        assertThat(ra.getFlashAttributes().get("messageType")).isEqualTo("error");
        verify(repo, never()).save(any(DanhMuc.class));
    }

    @Test
    void manufacturerSaveShouldUseCurrentRedirectOnDuplicateCreate() {
        AdminManufacturerController c = new AdminManufacturerController();
        HangSanXuatRepository repo = mock(HangSanXuatRepository.class);
        ReflectionTestUtils.setField(c, "hangRepo", repo);
        when(repo.existsByTenHang("Bandai")).thenReturn(true);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = c.save(HangSanXuat.builder().tenHang("Bandai").build(), ra);

        assertThat(view).isEqualTo("redirect:/admin/manufacturer");
        assertThat(ra.getFlashAttributes().get("messageType")).isEqualTo("error");
    }

    @Test
    void adminProductSaveShouldDefaultImageWhenMissing() {
        AdminProductController c = new AdminProductController();
        MoHinhRepository repo = mock(MoHinhRepository.class);
        DanhMucRepository danhMucRepo = mock(DanhMucRepository.class);
        HangSanXuatRepository hangRepo = mock(HangSanXuatRepository.class);
        LoaiHangRepository loaiHangRepo = mock(LoaiHangRepository.class);
        ReflectionTestUtils.setField(c, "moHinhRepo", repo);
        ReflectionTestUtils.setField(c, "danhMucRepo", danhMucRepo);
        ReflectionTestUtils.setField(c, "hangRepo", hangRepo);
        ReflectionTestUtils.setField(c, "loaiHangRepo", loaiHangRepo);
        when(repo.findById("P1")).thenReturn(Optional.empty());
        when(danhMucRepo.findByTenDanhMuc("Chưa phân loại"))
                .thenReturn(Optional.of(DanhMuc.builder().maDanhMuc(1).tenDanhMuc("Chưa phân loại").build()));
        when(hangRepo.findByTenHang("Không"))
                .thenReturn(Optional.of(HangSanXuat.builder().maHang(1).tenHang("Không").build()));
        when(repo.save(any(MoHinh.class))).thenAnswer(inv -> inv.getArgument(0));
        RedirectAttributes ra = new RedirectAttributesModelMap();
        MoHinh mh = MoHinh.builder().maMoHinh("P1").tenMoHinh("RX").hinhAnh("").build();

        String view = c.save(mh, null, null, null, ra);

        assertThat(view).isEqualTo("redirect:/admin/products");
        assertThat(mh.getHinhAnh()).isEqualTo("banner-bg.jpg");
        assertThat(mh.getGiaBan()).isEqualTo(0L);
        assertThat(mh.getGiaVon()).isEqualTo(0L);
        assertThat(mh.getTonKho()).isEqualTo(0);
        assertThat(mh.getNhomHang()).isEqualTo("Không");
        assertThat(mh.getDonViTinh()).isEqualTo("Không");
        assertThat(mh.getMoTa()).isEqualTo("Không");
        assertThat(mh.getDanhMuc().getTenDanhMuc()).isEqualTo("Chưa phân loại");
        assertThat(mh.getHangSanXuat().getTenHang()).isEqualTo("Không");
        assertThat(mh.getCreatedAt()).isNotNull();
    }

    @Test
    void promotionSaveShouldRejectInvalidDateRange() {
        AdminPromotionController c = new AdminPromotionController();
        KhuyenMaiRepository repo = mock(KhuyenMaiRepository.class);
        ReflectionTestUtils.setField(c, "kmRepo", repo);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        KhuyenMai km = KhuyenMai.builder()
            .maCode("K1")
            .ngayBatDau(LocalDateTime.of(2026, 2, 20, 0, 0))
            .ngayKetThuc(LocalDateTime.of(2026, 2, 19, 0, 0))
            .build();

        String view = c.save(km, ra);

        assertThat(view).isEqualTo("redirect:/admin/promotions");
        assertThat(ra.getFlashAttributes().get("messageType")).isEqualTo("error");
        verify(repo, never()).save(any(KhuyenMai.class));
    }

    @Test
    void adminUserSaveShouldRejectDuplicateUsernameOnCreate() {
        AdminUserController c = new AdminUserController();
        TaiKhoanRepository repo = mock(TaiKhoanRepository.class);
        ReflectionTestUtils.setField(c, "taiKhoanRepository", repo);
        when(repo.existsByTenDangNhap("admin")).thenReturn(true);
        RedirectAttributes ra = new RedirectAttributesModelMap();
        TaiKhoan tk = TaiKhoan.builder().tenDangNhap("admin").build();

        String view = c.save(tk, ra);

        assertThat(view).isEqualTo("redirect:/admin/accounts");
        assertThat(ra.getFlashAttributes().get("messageType")).isEqualTo("error");
        verify(repo, never()).save(any(TaiKhoan.class));
    }

    @Test
    void adminUserToggleShouldSetErrorWhenUserMissing() {
        AdminUserController c = new AdminUserController();
        TaiKhoanRepository repo = mock(TaiKhoanRepository.class);
        ReflectionTestUtils.setField(c, "taiKhoanRepository", repo);
        when(repo.findById(99)).thenReturn(Optional.empty());
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = c.toggleStatus(99, ra);

        assertThat(view).isEqualTo("redirect:/admin/accounts");
        assertThat(ra.getFlashAttributes().get("messageType")).isEqualTo("error");
    }

    @Test
    void adminRoleSaveShouldRejectDuplicateCode() {
        VaiTroService roleService = mock(VaiTroService.class);
        VaiTroRepository roleRepo = mock(VaiTroRepository.class);
        TaiKhoanRepository taiKhoanRepo = mock(TaiKhoanRepository.class);
        AuthService authService = mock(AuthService.class);
        AdminRoleController c = new AdminRoleController(roleService, roleRepo, taiKhoanRepo, authService);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        when(roleService.normalizeCode("CEO")).thenReturn("CEO");
        when(roleService.codeExistsForAnotherRole("CEO", null)).thenReturn(true);

        String view = c.save(VaiTro.builder().code("CEO").tenHienThi("CEO").moTa("Dieu hanh").build(), ra);

        assertThat(view).isEqualTo("redirect:/admin/roles");
        assertThat(ra.getFlashAttributes().get("messageType")).isEqualTo("error");
        verify(roleService, never()).save(any(VaiTro.class));
    }

    @Test
    void adminRoleDeleteShouldBlockSystemRole() {
        VaiTroService roleService = mock(VaiTroService.class);
        VaiTroRepository roleRepo = mock(VaiTroRepository.class);
        TaiKhoanRepository taiKhoanRepo = mock(TaiKhoanRepository.class);
        AuthService authService = mock(AuthService.class);
        AdminRoleController c = new AdminRoleController(roleService, roleRepo, taiKhoanRepo, authService);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        when(roleService.findById(1)).thenReturn(VaiTro.builder()
                .maVaiTro(1)
                .code(TaiKhoan.ROLE_ADMIN)
                .tenHienThi("Admin")
                .moTa("Quan tri")
                .laVaiTroHeThong(true)
                .choPhepTruyCapAdmin(true)
                .build());

        String view = c.delete(1, ra);

        assertThat(view).isEqualTo("redirect:/admin/roles");
        assertThat(ra.getFlashAttributes().get("messageType")).isEqualTo("error");
        verify(roleRepo, never()).deleteById(anyInt());
    }

    @Test
    void adminVariantIndexShouldRedirectWhenParentMissing() {
        AdminVariantController c = new AdminVariantController();
        MoHinhRepository moHinhRepo = mock(MoHinhRepository.class);
        ReflectionTestUtils.setField(c, "moHinhRepo", moHinhRepo);
        when(moHinhRepo.findById("NOTFOUND")).thenReturn(Optional.empty());

        assertThat(c.index("NOTFOUND", new ExtendedModelMap())).isEqualTo("redirect:/admin/products");
    }

    @Test
    void adminVariantSaveShouldAutoGenerateSku() {
        AdminVariantController c = new AdminVariantController();
        BienTheMoHinhRepository repo = mock(BienTheMoHinhRepository.class);
        ReflectionTestUtils.setField(c, "variantRepo", repo);
        when(repo.save(any(BienTheMoHinh.class))).thenAnswer(inv -> inv.getArgument(0));
        RedirectAttributes ra = new RedirectAttributesModelMap();
        BienTheMoHinh bt = BienTheMoHinh.builder().sku("").build();

        String view = c.save(bt, "P1", ra);

        assertThat(view).isEqualTo("redirect:/admin/variants/P1");
        assertThat(bt.getSku()).startsWith("SKU-P1-");
    }

    @Test
    void adminDashboardShouldPrepareMonthlyChartData() {
        AdminDashboardController c = new AdminDashboardController();
        AdminDashboardService dashboard = mock(AdminDashboardService.class);
        ReportService report = mock(ReportService.class);
        ReflectionTestUtils.setField(c, "dashboardService", dashboard);
        ReflectionTestUtils.setField(c, "reportService", report);
        when(report.thongKeTongQuan(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ThongKeTongQuanDTO(0L, 0L, 0L, 0L, 0.0, 0L, 0L, 0L));
        List<Object[]> byMonth = new ArrayList<>();
        byMonth.add(new Object[] {1, 1000L, 2L});
        when(report.thongKeDoanhThuTheoThang(anyInt())).thenReturn(byMonth);
        when(dashboard.getTotalOrders()).thenReturn(7L);
        when(dashboard.getTotalProducts()).thenReturn(12L);
        when(dashboard.getLowStockProducts()).thenReturn(3L);
        when(dashboard.getRecentOrderItems()).thenReturn(List.of(
                new AdminRecentOrderItemDTO(1, "Khach A", 1000L, "Đặt hàng", "Chưa nhận tiền")
        ));
        when(dashboard.getCancelledOrders()).thenReturn(1L);

        Model model = new ExtendedModelMap();
        String view = c.dashboard(model);

        assertThat(view).isEqualTo("admin/dashboard");
        assertThat(model.getAttribute("totalOrders")).isEqualTo(7L);
        assertThat(model.getAttribute("totalProducts")).isEqualTo(12L);
        assertThat(model.getAttribute("lowStockProducts")).isEqualTo(3L);
        assertThat(model.getAttribute("chartRevenues")).isEqualTo("[1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]");
        assertThat(model.getAttribute("chartOrders")).isEqualTo("[2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]");
    }

    @Test
    void adminCategoryApiShouldReturnNotFoundWhenMissing() {
        AdminCategoryController c = new AdminCategoryController();
        DanhMucRepository repo = mock(DanhMucRepository.class);
        ReflectionTestUtils.setField(c, "danhMucRepo", repo);
        when(repo.findById(123)).thenReturn(Optional.empty());

        assertThat(c.getApi(123).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void adminPromotionApiShouldReturnOkWhenFound() {
        AdminPromotionController c = new AdminPromotionController();
        KhuyenMaiRepository repo = mock(KhuyenMaiRepository.class);
        ReflectionTestUtils.setField(c, "kmRepo", repo);
        KhuyenMai km = KhuyenMai.builder().maCode("PROMO").build();
        when(repo.findById("PROMO")).thenReturn(Optional.of(km));

        assertThat(c.getApi("PROMO").getBody()).isEqualTo(km);
    }
}
