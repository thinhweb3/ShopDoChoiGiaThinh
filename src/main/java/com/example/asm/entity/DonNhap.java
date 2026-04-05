package com.example.asm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DonNhap")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DonNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonNhap")
    private Integer maDonNhap;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaTaiKhoan", foreignKey = @ForeignKey(name = "FK_DonNhap_TK"))
    private TaiKhoan taiKhoan;

    @Column(name = "NgayNhap", nullable = false)
    private LocalDateTime ngayNhap;

    @Column(name = "TongTienNhap", nullable = false, precision = 18, scale = 0)
    private Long tongTienNhap;

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @OneToMany(mappedBy = "donNhap", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChiTietNhap> chiTiets = new ArrayList<>();
}
