package com.example.asm.repository;

import com.example.asm.entity.LoaiHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoaiHangRepository extends JpaRepository<LoaiHang, Integer> {
    Optional<LoaiHang> findByTenLoaiHangIgnoreCase(String tenLoaiHang);
    boolean existsByTenLoaiHangIgnoreCase(String tenLoaiHang);
    List<LoaiHang> findAllByOrderByTenLoaiHangAsc();
    List<LoaiHang> findByTrangThaiTrueOrderByTenLoaiHangAsc();
}
