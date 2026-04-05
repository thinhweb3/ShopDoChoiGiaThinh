package com.example.asm.repository;

import com.example.asm.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    Optional<DanhMuc> findByTenDanhMuc(String tenDanhMuc);
    boolean existsByTenDanhMuc(String tenDanhMuc);
}
