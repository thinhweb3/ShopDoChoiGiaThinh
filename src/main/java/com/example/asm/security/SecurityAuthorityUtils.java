package com.example.asm.security;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;

public final class SecurityAuthorityUtils {

    public static final String ADMIN_PANEL_AUTHORITY = "ADMIN_PANEL";
    public static final String PERMISSION_PREFIX = "PERM_";

    private SecurityAuthorityUtils() {
    }

    public static Collection<? extends GrantedAuthority> buildAuthorities(TaiKhoan taiKhoan) {
        LinkedHashSet<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        if (taiKhoan == null) {
            return authorities;
        }

        for (VaiTro role : taiKhoan.getSortedRoles()) {
            String roleCode = normalizeRoleCode(role);
            if (StringUtils.hasText(roleCode)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
            }
            boolean adminPanelAccess = Boolean.TRUE.equals(role.getChoPhepTruyCapAdmin());
            if (adminPanelAccess) {
                authorities.add(new SimpleGrantedAuthority(ADMIN_PANEL_AUTHORITY));
            }

            Collection<String> permissionCodes = role.getPermissionCodes();
            if (adminPanelAccess && permissionCodes.isEmpty()) {
                // Backward-compatible fallback for seeded ADMIN roles that have not been
                // expanded into granular permissions yet.
                permissionCodes = RolePermission.allCodes();
            }

            for (String permissionCode : permissionCodes) {
                String authority = permissionAuthority(permissionCode);
                if (StringUtils.hasText(authority)) {
                    authorities.add(new SimpleGrantedAuthority(authority));
                }
            }
        }
        return authorities;
    }

    public static String permissionAuthority(String permissionCode) {
        String normalizedCode = RolePermission.normalizeCode(permissionCode);
        return normalizedCode == null ? null : PERMISSION_PREFIX + normalizedCode;
    }

    private static String normalizeRoleCode(VaiTro role) {
        if (role == null || !StringUtils.hasText(role.getCode())) {
            return null;
        }
        return role.getCode().trim().toUpperCase();
    }
}
