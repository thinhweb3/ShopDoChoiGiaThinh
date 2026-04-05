package com.example.asm.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "BienTheDoChoi")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BienTheMoHinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBienTheDoChoi")
    private Integer maBienThe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaDoChoi", foreignKey = @ForeignKey(name = "FK_BienThe_DoChoi"))
    @ToString.Exclude 
    private MoHinh moHinh;

    @Column(name = "KichThuoc", nullable = false)
    private String kichThuoc; 

    @Column(name = "GiaBan", nullable = false)
    private Long giaBan;

    @Column(name = "SoLuongTon", nullable = false)
    private Integer soLuongTon;

    @Column(name = "SKU")
    private String sku;

    @Column(name = "TinhTrang", length = 50)
    private String tinhTrang;

    @OneToMany(mappedBy = "bienThe", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<GioHang> gioHangs = new ArrayList<>();

    @OneToMany(mappedBy = "bienThe", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<ChiTietDonHang> chiTietDonHangs = new ArrayList<>();

    @OneToMany(mappedBy = "bienThe", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<ChiTietNhap> chiTietNhaps = new ArrayList<>();
}
