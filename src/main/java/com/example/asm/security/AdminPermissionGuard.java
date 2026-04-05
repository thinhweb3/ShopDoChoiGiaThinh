package com.example.asm.security;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.DanhMuc;
import com.example.asm.entity.HangSanXuat;
import com.example.asm.entity.KhuyenMai;
import com.example.asm.entity.MoHinh;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import com.example.asm.repository.KhuyenMaiRepository;
import com.example.asm.repository.MoHinhRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component("adminPermissionGuard")
public class AdminPermissionGuard {

    private final MoHinhRepository moHinhRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;

    public AdminPermissionGuard(MoHinhRepository moHinhRepository,
                                KhuyenMaiRepository khuyenMaiRepository) {
        this.moHinhRepository = moHinhRepository;
        this.khuyenMaiRepository = khuyenMaiRepository;
    }

    public boolean hasPermission(String permissionCode) {
        String authority = SecurityAuthorityUtils.permissionAuthority(permissionCode);
        if (authority == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }

    public boolean hasAnyPermission(String... permissionCodes) {
        return permissionCodes != null && Arrays.stream(permissionCodes).anyMatch(this::hasPermission);
    }

    public boolean hasAnyAdminPermission() {
        return hasAnyPermission(RolePermission.allCodes().toArray(String[]::new));
    }

    public boolean canAccessCategorySection() {
        return hasAnyPermission(
                RolePermission.CATEGORY_CREATE.getCode(),
                RolePermission.CATEGORY_UPDATE.getCode(),
                RolePermission.CATEGORY_DELETE.getCode()
        );
    }

    public boolean canAccessManufacturerSection() {
        return hasAnyPermission(
                RolePermission.MANUFACTURER_CREATE.getCode(),
                RolePermission.MANUFACTURER_UPDATE.getCode(),
                RolePermission.MANUFACTURER_DELETE.getCode()
        );
    }

    public boolean canAccessProductSection() {
        return hasAnyPermission(
                RolePermission.PRODUCT_CREATE.getCode(),
                RolePermission.PRODUCT_UPDATE.getCode(),
                RolePermission.PRODUCT_DELETE.getCode(),
                RolePermission.PRODUCT_VARIANT_MANAGE.getCode()
        );
    }

    public boolean canAccessOrderSection() {
        return hasAnyPermission(
                RolePermission.ORDER_CONFIRM.getCode(),
                RolePermission.ORDER_CANCEL.getCode(),
                RolePermission.ORDER_DELETE.getCode()
        );
    }

    public boolean canAccessAccountSection() {
        return hasAnyPermission(
                RolePermission.ACCOUNT_CREATE.getCode(),
                RolePermission.ACCOUNT_UPDATE.getCode(),
                RolePermission.ACCOUNT_LOCK.getCode(),
                RolePermission.ACCOUNT_DELETE.getCode()
        );
    }

    public boolean canAccessRoleSection() {
        return hasAnyPermission(
                RolePermission.ROLE_CREATE.getCode(),
                RolePermission.ROLE_UPDATE.getCode(),
                RolePermission.ROLE_DELETE.getCode()
        );
    }

    public boolean canAccessPromotionSection() {
        return hasAnyPermission(
                RolePermission.PROMOTION_CREATE.getCode(),
                RolePermission.PROMOTION_UPDATE.getCode(),
                RolePermission.PROMOTION_DELETE.getCode()
        );
    }

    public boolean canAccessInboundSection() {
        return hasAnyPermission(
                RolePermission.INBOUND_CREATE.getCode(),
                RolePermission.INBOUND_UPDATE.getCode(),
                RolePermission.INBOUND_DELETE.getCode()
        );
    }

    public boolean canSaveCategory(DanhMuc danhMuc) {
        return hasPermission(danhMuc != null && danhMuc.getMaDanhMuc() != null
                ? RolePermission.CATEGORY_UPDATE.getCode()
                : RolePermission.CATEGORY_CREATE.getCode());
    }

    public boolean canSaveManufacturer(HangSanXuat manufacturer) {
        return hasPermission(manufacturer != null && manufacturer.getMaHang() != null
                ? RolePermission.MANUFACTURER_UPDATE.getCode()
                : RolePermission.MANUFACTURER_CREATE.getCode());
    }

    public boolean canSaveProduct(MoHinh product) {
        boolean isUpdate = product != null
                && StringUtils.hasText(product.getMaMoHinh())
                && moHinhRepository.existsById(product.getMaMoHinh());
        return hasPermission(isUpdate
                ? RolePermission.PRODUCT_UPDATE.getCode()
                : RolePermission.PRODUCT_CREATE.getCode());
    }

    public boolean canSaveVariant(BienTheMoHinh variant) {
        return hasPermission(RolePermission.PRODUCT_VARIANT_MANAGE.getCode());
    }

    public boolean canSavePromotion(KhuyenMai promotion) {
        boolean isUpdate = promotion != null
                && StringUtils.hasText(promotion.getMaCode())
                && khuyenMaiRepository.existsById(promotion.getMaCode());
        return hasPermission(isUpdate
                ? RolePermission.PROMOTION_UPDATE.getCode()
                : RolePermission.PROMOTION_CREATE.getCode());
    }

    public boolean canSaveAccount(TaiKhoan account) {
        return hasPermission(account != null && account.getMaTaiKhoan() != null
                ? RolePermission.ACCOUNT_UPDATE.getCode()
                : RolePermission.ACCOUNT_CREATE.getCode());
    }

    public boolean canSaveRole(VaiTro role) {
        return hasPermission(role != null && role.getMaVaiTro() != null
                ? RolePermission.ROLE_UPDATE.getCode()
                : RolePermission.ROLE_CREATE.getCode());
    }
}
