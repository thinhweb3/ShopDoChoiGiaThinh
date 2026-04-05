package com.example.asm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ChiTietNhap")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChiTietNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTietNhap")
    private Integer maChiTietNhap;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaDonNhap", foreignKey = @ForeignKey(name = "FK_CTN_DonNhap"))
    private DonNhap donNhap;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaBienTheDoChoi", foreignKey = @ForeignKey(name = "FK_CTN_BienTheDoChoi"))
    private BienTheMoHinh bienThe;

    @Column(name = "SoLuongNhap", nullable = false)
    private Integer soLuongNhap;

    @Column(name = "GiaNhap", nullable = false, precision = 18, scale = 0)
    private Long giaNhap;
}
