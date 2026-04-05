package com.example.asm.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "HangSanXuat",
        uniqueConstraints = @UniqueConstraint(name = "UQ_HangSanXuat_Ten", columnNames = "TenHang")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HangSanXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHang")
    private Integer maHang;

    @Column(name = "TenHang", nullable = false, length = 150, unique = true)
    private String tenHang;

    @Column(name = "Website")
    private String website;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @OneToMany(mappedBy = "hangSanXuat", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MoHinh> moHinhs = new ArrayList<>();
}
