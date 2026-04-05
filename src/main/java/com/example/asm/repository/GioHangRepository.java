package com.example.asm.repository;

import com.example.asm.entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    List<GioHang> findByTaiKhoan_MaTaiKhoan(Integer maTaiKhoan);
    Optional<GioHang> findByTaiKhoan_MaTaiKhoanAndBienThe_MaBienThe(Integer maTaiKhoan, Integer maBienThe);
    void deleteByTaiKhoan_MaTaiKhoan(Integer maTaiKhoan);
}
