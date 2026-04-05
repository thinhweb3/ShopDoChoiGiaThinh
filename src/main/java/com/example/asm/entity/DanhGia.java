package com.example.asm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "DanhGia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhGia")
    private Integer maDanhGia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaTaiKhoan", foreignKey = @ForeignKey(name = "FK_DanhGia_TK"))
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaDoChoi", foreignKey = @ForeignKey(name = "FK_DanhGia_DoChoi"))
    private MoHinh moHinh;

    @Column(name = "SoSao", nullable = false)
    private Byte soSao;

    @Column(name = "NhanXet", columnDefinition = "TEXT")
    private String nhanXet;

    @Column(name = "NgayDanhGia", nullable = false)
    private LocalDateTime ngayDanhGia;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai;
}
