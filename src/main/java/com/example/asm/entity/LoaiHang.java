package com.example.asm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "LoaiHang",
        uniqueConstraints = @UniqueConstraint(name = "UQ_LoaiHang_Ten", columnNames = "TenLoaiHang")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoaiHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLoaiHang")
    private Integer maLoaiHang;

    @Column(name = "TenLoaiHang", nullable = false, length = 150, unique = true)
    private String tenLoaiHang;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai;
}
