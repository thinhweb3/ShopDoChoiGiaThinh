package com.example.asm.repository;

import com.example.asm.entity.HangSanXuat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HangSanXuatRepository extends JpaRepository<HangSanXuat, Integer> {
    Optional<HangSanXuat> findByTenHang(String tenHang);
    boolean existsByTenHang(String tenHang);
}
