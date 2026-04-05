package com.example.asm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "YeuThich",
        uniqueConstraints = @UniqueConstraint(name = "UQ_YeuThich", columnNames = {"MaTaiKhoan", "MaDoChoi"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class YeuThich {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaYeuThich")
    private Integer maYeuThich;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaTaiKhoan", foreignKey = @ForeignKey(name = "FK_YeuThich_TK"))
    @ToString.Exclude // <--- QUAN TRỌNG: Thêm dòng này để tránh đơ web
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaDoChoi", foreignKey = @ForeignKey(name = "FK_YeuThich_DoChoi"))
    @ToString.Exclude // <--- QUAN TRỌNG: Thêm dòng này để tránh đơ web
    private MoHinh moHinh;

    @Column(name = "NgayThem", nullable = false)
    private LocalDateTime ngayThem;
}
