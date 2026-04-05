package com.example.asm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.asm.security.RolePermission;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "TaiKhoan",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_TaiKhoan_TenDangNhap", columnNames = "TenDangNhap"),
                @UniqueConstraint(name = "UQ_TaiKhoan_Email", columnNames = "Email")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaiKhoan {

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    private static final Map<String, Integer> ROLE_PRIORITY = Map.of(
            ROLE_SUPER_ADMIN, 0,
            ROLE_ADMIN, 1,
            ROLE_USER, 2
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTaiKhoan")
    private Integer maTaiKhoan;

    @Column(name = "TenDangNhap", nullable = false, length = 50)
    private String tenDangNhap;

    @Column(name = "MatKhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "Email", nullable = false, length = 100)
    private String email;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "Avatar", nullable = false, length = 255)
    private String avatar;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "TaiKhoanRole",
            joinColumns = @JoinColumn(name = "MaTaiKhoan", foreignKey = @ForeignKey(name = "FK_TaiKhoanRole_TaiKhoan")),
            inverseJoinColumns = @JoinColumn(name = "MaVaiTro", foreignKey = @ForeignKey(name = "FK_TaiKhoanRole_VaiTro"))
    )
    @Builder.Default
    private Set<VaiTro> roles = new LinkedHashSet<>();

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai;

    @Column(name = "NgayTao", nullable = false)
    private LocalDateTime ngayTao;

    // -------- Relations --------
    @OneToMany(mappedBy = "taiKhoan", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<DonHang> donHangs = new ArrayList<>();

    @OneToMany(mappedBy = "taiKhoan", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<DonNhap> donNhaps = new ArrayList<>();

    @OneToMany(mappedBy = "taiKhoan", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<GioHang> gioHangs = new ArrayList<>();

    @OneToMany(mappedBy = "taiKhoan", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<YeuThich> yeuThichs = new ArrayList<>();

    @OneToMany(mappedBy = "taiKhoan", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<DanhGia> danhGias = new ArrayList<>();

    @Transient
    public String getRole() {
        return getSortedRoleCodes().stream().findFirst().orElse(null);
    }

    public void setRole(String roleCode) {
        throw new UnsupportedOperationException("setRole(String) không còn được hỗ trợ. Hãy gán vai trò qua VaiTroService.");
    }

    public void replaceRoles(List<VaiTro> requestedRoles) {
        LinkedHashSet<VaiTro> normalizedRoles = requestedRoles == null
                ? new LinkedHashSet<>()
                : requestedRoles.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        this.roles.clear();
        this.roles.addAll(normalizedRoles);
    }

    @Transient
    public List<VaiTro> getSortedRoles() {
        return roles == null
                ? List.of()
                : roles.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt((VaiTro role) -> ROLE_PRIORITY.getOrDefault(normalizeRoleCode(role), 100))
                        .thenComparing(role -> safeText(role.getTenHienThi()))
                        .thenComparing(role -> safeText(role.getCode())))
                .toList();
    }

    @Transient
    public String getRoleDisplayText() {
        List<String> labels = getSortedRoles().stream()
                .map(VaiTro::getTenHienThi)
                .toList();
        return labels.isEmpty() ? "Khách hàng" : String.join(", ", labels);
    }

    @Transient
    public List<Integer> getSortedRoleIds() {
        return getSortedRoles().stream()
                .map(VaiTro::getMaVaiTro)
                .filter(Objects::nonNull)
                .toList();
    }

    @Transient
    public List<String> getSortedRoleCodes() {
        return getSortedRoles().stream()
                .map(VaiTro::getCode)
                .filter(Objects::nonNull)
                .map(code -> code.trim().toUpperCase(Locale.ROOT))
                .toList();
    }

    @Transient
    public boolean hasRole(String roleCode) {
        if (roleCode == null || roles == null) {
            return false;
        }
        String expectedCode = roleCode.trim().toUpperCase(Locale.ROOT);
        return roles.stream().anyMatch(role -> expectedCode.equals(normalizeRoleCode(role)));
    }

    @Transient
    public boolean hasAdminPanelAccess() {
        return getSortedRoles().stream().anyMatch(role -> Boolean.TRUE.equals(role.getChoPhepTruyCapAdmin()));
    }

    @Transient
    public List<String> getPermissionCodes() {
        return getSortedRoles().stream()
                .flatMap(role -> role.getPermissionCodes().stream())
                .map(RolePermission::normalizeCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transient
    public boolean hasPermission(String permissionCode) {
        String normalizedCode = RolePermission.normalizeCode(permissionCode);
        return normalizedCode != null && getPermissionCodes().contains(normalizedCode);
    }

    private String normalizeRoleCode(VaiTro role) {
        return role == null || role.getCode() == null
                ? null
                : role.getCode().trim().toUpperCase(Locale.ROOT);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
