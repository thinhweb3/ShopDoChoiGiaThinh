package com.example.asm.repository;

import com.example.asm.entity.YeuThich;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface YeuThichRepository extends JpaRepository<YeuThich, Integer> {
    List<YeuThich> findByTaiKhoan_MaTaiKhoan(Integer maTaiKhoan);

    boolean existsByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(Integer maTaiKhoan, Integer maMoHinh);

    Optional<YeuThich> findByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(Integer maTaiKhoan, Integer maMoHinh);

    void deleteByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(Integer maTaiKhoan, Integer maMoHinh);

    boolean existsByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(Integer maTaiKhoan, String maMoHinh);

    Optional<YeuThich> findByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(Integer maTaiKhoan, String maMoHinh);

    void deleteByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(Integer maTaiKhoan, String maMoHinh);
}
