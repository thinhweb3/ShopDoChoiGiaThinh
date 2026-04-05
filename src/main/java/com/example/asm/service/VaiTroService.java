package com.example.asm.service;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import com.example.asm.repository.VaiTroRepository;
import com.example.asm.security.RolePermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VaiTroService {

    private static final Map<String, Integer> SYSTEM_ROLE_PRIORITY = Map.of(
            TaiKhoan.ROLE_SUPER_ADMIN, 0,
            TaiKhoan.ROLE_ADMIN, 1,
            TaiKhoan.ROLE_USER, 2
    );

    private final VaiTroRepository vaiTroRepository;

    public VaiTroService(VaiTroRepository vaiTroRepository) {
        this.vaiTroRepository = vaiTroRepository;
    }

    public List<VaiTro> findAllOrdered() {
        return vaiTroRepository.findAll().stream()
                .sorted(roleComparator())
                .toList();
    }

    public VaiTro findById(Integer id) {
        return id == null ? null : vaiTroRepository.findById(id).orElse(null);
    }

    public VaiTro getRequiredUserRole() {
        return vaiTroRepository.findByCodeIgnoreCase(TaiKhoan.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Chưa cấu hình vai trò mặc định USER trong bảng VaiTro."));
    }

    public List<VaiTro> resolveRolesByIds(Collection<Integer> roleIds, boolean fallbackToUserRole) {
        Set<Integer> normalizedIds = roleIds == null
                ? Set.of()
                : roleIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedIds.isEmpty()) {
            return fallbackToUserRole ? List.of(getRequiredUserRole()) : List.of();
        }

        Map<Integer, VaiTro> rolesById = vaiTroRepository.findAllById(normalizedIds).stream()
                .collect(Collectors.toMap(VaiTro::getMaVaiTro, Function.identity()));

        LinkedHashSet<VaiTro> resolved = normalizedIds.stream()
                .map(rolesById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (resolved.isEmpty() && fallbackToUserRole) {
            resolved.add(getRequiredUserRole());
        }

        return resolved.stream().sorted(roleComparator()).toList();
    }

    @Transactional
    public VaiTro save(VaiTro vaiTro) {
        return vaiTroRepository.save(vaiTro);
    }

    public boolean existsByCode(String code) {
        return vaiTroRepository.existsByCodeIgnoreCase(code);
    }

    public boolean codeExistsForAnotherRole(String code, Integer excludedRoleId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        return vaiTroRepository.findByCodeIgnoreCase(code)
                .filter(role -> !Objects.equals(role.getMaVaiTro(), excludedRoleId))
                .isPresent();
    }

    public String normalizeCode(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            return null;
        }

        String normalized = rawCode.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "_")
                .replaceAll("[^A-Z0-9_]", "");

        normalized = normalized.replaceAll("_+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    public Set<String> resolvePermissionCodes(Collection<String> permissionCodes) {
        return RolePermission.normalizeCodes(permissionCodes);
    }

    public Map<String, List<RolePermission>> getPermissionGroups() {
        return RolePermission.groupedOptions();
    }

    @Transactional
    public void ensureSystemRoles() {
        ensureRole(
                TaiKhoan.ROLE_SUPER_ADMIN,
                "Siêu quản trị viên",
                "Toàn quyền hệ thống",
                true,
                true,
                RolePermission.allCodes()
        );
        ensureRole(
                TaiKhoan.ROLE_ADMIN,
                "Quản trị viên",
                "Quản trị nội dung và vận hành",
                true,
                true,
                RolePermission.adminDefaultCodes()
        );
        ensureRole(
                TaiKhoan.ROLE_USER,
                "Khách hàng",
                "Khách hàng mua hàng",
                false,
                true,
                Set.of()
        );
    }

    private void ensureRole(String code,
                            String displayName,
                            String description,
                            boolean adminAccess,
                            boolean systemRole,
                            Collection<String> permissionCodes) {
        VaiTro role = vaiTroRepository.findByCodeIgnoreCase(code)
                .orElseGet(() -> VaiTro.builder().code(code).build());

        role.setTenHienThi(displayName);
        role.setMoTa(description);
        role.setPermissionCodes(permissionCodes);
        role.setChoPhepTruyCapAdmin(adminAccess);
        role.setLaVaiTroHeThong(systemRole);
        vaiTroRepository.save(role);
    }

    private Comparator<VaiTro> roleComparator() {
        return Comparator
                .comparingInt((VaiTro role) -> SYSTEM_ROLE_PRIORITY.getOrDefault(normalizeCode(role.getCode()), 100))
                .thenComparing(role -> safeText(role.getTenHienThi()))
                .thenComparing(role -> safeText(role.getCode()));
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
