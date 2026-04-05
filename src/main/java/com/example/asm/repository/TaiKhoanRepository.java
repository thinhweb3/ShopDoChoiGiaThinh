package com.example.asm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.asm.entity.TaiKhoan;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    Optional<TaiKhoan> findByEmail(String email);
    Optional<TaiKhoan> findByEmailIgnoreCase(String email);

    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmail(String email);

    @Query("""
        select distinct tk
        from TaiKhoan tk
        join tk.roles roleValue
        where upper(roleValue.code) in :roleCodes
        order by tk.ngayTao desc
    """)
    List<TaiKhoan> findByAnyRoleOrderByNgayTaoDesc(@Param("roleCodes") List<String> roleCodes, Pageable pageable);

    long countByRoles_MaVaiTro(Integer maVaiTro);
}
