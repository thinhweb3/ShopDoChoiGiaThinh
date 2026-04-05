package com.example.asm.entity;

import com.example.asm.security.RolePermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "VaiTro",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_VaiTro_Code", columnNames = "Code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "maVaiTro")
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaVaiTro")
    private Integer maVaiTro;

    @Column(name = "Code", nullable = false, length = 50)
    private String code;

    @Column(name = "TenHienThi", nullable = false, length = 100)
    private String tenHienThi;

    @Column(name = "MoTa", nullable = false, length = 255)
    private String moTa;

    @Column(name = "Quyen", length = 2000)
    private String quyen;

    @Column(name = "ChoPhepTruyCapAdmin", nullable = false)
    private Boolean choPhepTruyCapAdmin;

    @Column(name = "LaVaiTroHeThong", nullable = false)
    private Boolean laVaiTroHeThong;

    @Transient
    public List<String> getPermissionCodes() {
        if (quyen == null || quyen.isBlank()) {
            return List.of();
        }
        return RolePermission.normalizeCodes(Arrays.stream(quyen.split("[,;\\r\\n]+"))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList())
                .stream()
                .toList();
    }

    public void setPermissionCodes(Collection<String> permissionCodes) {
        this.quyen = RolePermission.normalizeCodes(permissionCodes).stream()
                .collect(Collectors.joining(","));
    }

    @Transient
    public List<String> getPermissionLabels() {
        return RolePermission.labelsFor(getPermissionCodes());
    }

    @Transient
    public String getPermissionSummaryText() {
        List<String> labels = getPermissionLabels();
        return labels.isEmpty() ? "Chưa cấu hình quyền thao tác" : String.join(", ", labels);
    }

    @Transient
    public boolean hasPermission(String permissionCode) {
        String normalizedCode = RolePermission.normalizeCode(permissionCode);
        return normalizedCode != null && getPermissionCodes().contains(normalizedCode);
    }
}
