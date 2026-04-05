package com.example.asm.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "KhuyenMai")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KhuyenMai {

	@Id
	@Column(name = "MaKhuyenMai", length = 20)
	private String maCode;

    @Column(name = "TenKhuyenMai", nullable = false)
    private String tenKhuyenMai;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "PhanTramGiam")
    private Integer phanTramGiam;

    @Column(name = "GiamToiDa", precision = 18, scale = 0)
    private Long giamToiDa;

    @Column(name = "SoTienGiam", precision = 18, scale = 0)
    private Long soTienGiam;

    @Column(name = "DonToiThieu", precision = 18, scale = 0)
    private Long donToiThieu;

    @Column(name = "NgayBatDau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "NgayKetThuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai;

    @OneToMany(mappedBy = "khuyenMai", fetch = FetchType.LAZY)
    @Builder.Default
    private List<DonHang> donHangs = new ArrayList<>();
}
