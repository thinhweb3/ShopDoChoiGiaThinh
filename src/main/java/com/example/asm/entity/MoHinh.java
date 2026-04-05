package com.example.asm.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "DoChoi")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MoHinh {

    @Id
    @Column(name = "MaDoChoi", length = 50)
    private String maMoHinh;

    @Column(name = "TenDoChoi", nullable = false, length = 300)
    private String tenMoHinh;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaDanhMuc")
    @ToString.Exclude 
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHang")
    @ToString.Exclude 
    private HangSanXuat hangSanXuat;

    @Column(name = "HinhAnh")
    private String hinhAnh;

    @Column(name = "TyLe")
    private String tyLe;

    @Column(name = "ChatLieu")
    private String chatLieu;

    @Column(name = "NhomHang", length = 150)
    private String nhomHang;

    @Column(name = "GiaBan", precision = 18, scale = 0)
    private Long giaBan;

    @Column(name = "GiaVon", precision = 18, scale = 0)
    private Long giaVon;

    @Column(name = "TonKho")
    private Integer tonKho;

    @Column(name = "TonKhoToiThieu")
    private Integer tonKhoToiThieu;

    @Column(name = "TonKhoToiDa")
    private Integer tonKhoToiDa;

    @Column(name = "DonViTinh", length = 50)
    private String donViTinh;

    @Column(name = "QuyCach", length = 150)
    private String quyCach;

    @Column(name = "DangKy")
    private Boolean dangKy;

    @Column(name = "DuocBan")
    private Boolean duocBan;

    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "MauGhiChu", length = 255)
    private String mauGhiChu;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "moHinh", fetch = FetchType.LAZY) 
    @Builder.Default
    @ToString.Exclude 
    private List<BienTheMoHinh> bienThes = new ArrayList<>();

    @OneToMany(mappedBy = "moHinh", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude 
    private List<YeuThich> yeuThichs = new ArrayList<>();

    @OneToMany(mappedBy = "moHinh", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude 
    private List<DanhGia> danhGias = new ArrayList<>();
}
