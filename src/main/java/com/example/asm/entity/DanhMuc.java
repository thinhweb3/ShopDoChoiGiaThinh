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
        name = "DanhMuc",
        uniqueConstraints = @UniqueConstraint(name = "UQ_DanhMuc_Ten", columnNames = "TenDanhMuc")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhMuc")
    private Integer maDanhMuc;

    @Column(name = "TenDanhMuc", nullable = false, length = 100, unique = true)
    private String tenDanhMuc;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @OneToMany(mappedBy = "danhMuc", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MoHinh> moHinhs = new ArrayList<>();
}
