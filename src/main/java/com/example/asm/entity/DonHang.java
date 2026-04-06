package com.example.asm.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Entity
@Table(name = "DonHang")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonHang")
    private Integer maDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKhuyenMai")
    private KhuyenMai khuyenMai;

    @Column(name = "NgayDat", nullable = false)
    private LocalDateTime ngayDat;

    @Column(name = "TongTienHang", precision = 18, scale = 0)
    private Long tongTienHang;

    @Column(name = "PhiVanChuyen", precision = 18, scale = 0)
    private Long phiVanChuyen;

    @Column(name = "TienGiamGia", precision = 18, scale = 0)
    private Long tienGiamGia;

    @Column(name = "TongTien", nullable = false, precision = 18, scale = 0)
    private Long tongTien;

    @Column(name = "DiaChiGiaoHang", nullable = false, length = 255)
    private String diaChiGiaoHang; 

    @Column(name = "TenNguoiNhan", length = 255)
    private String tenNguoiNhan;

    @Column(name = "SoDienThoaiNhan", length = 20)
    private String soDienThoaiNhan;

    @Column(name = "EmailNguoiNhan", length = 100)
    private String emailNguoiNhan;

    @Column(name = "GhiChu", length = 1000)
    private String ghiChu;

    @Column(name = "TrangThai", length = 50)
    private String trangThai; 

    @Column(name = "TrangThaiThanhToan", length = 50)
    private String trangThaiThanhToan;

    @OneToMany(mappedBy = "donHang", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChiTietDonHang> chiTiets = new ArrayList<>();
}
