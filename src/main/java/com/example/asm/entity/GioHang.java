package com.example.asm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "GioHang",
        uniqueConstraints = @UniqueConstraint(name = "UQ_GioHang", columnNames = {"MaTaiKhoan", "MaBienTheDoChoi"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGioHang")
    private Integer maGioHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaTaiKhoan", foreignKey = @ForeignKey(name = "FK_GioHang_TK"))
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaBienTheDoChoi", foreignKey = @ForeignKey(name = "FK_GioHang_BienTheDoChoi"))
    private BienTheMoHinh bienThe;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "NgayCapNhat", nullable = false)
    private LocalDateTime ngayCapNhat;
}
