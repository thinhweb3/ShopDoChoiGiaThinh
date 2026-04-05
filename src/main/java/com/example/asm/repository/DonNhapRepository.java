package com.example.asm.repository;

import com.example.asm.entity.DonNhap;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface DonNhapRepository extends JpaRepository<DonNhap, Integer> {
    List<DonNhap> findByTaiKhoan_MaTaiKhoan(Integer maTaiKhoan);
    List<DonNhap> findByNgayNhapBetween(LocalDateTime from, LocalDateTime to);
    List<DonNhap> findByNgayNhapBetween(LocalDateTime from, LocalDateTime to, Sort sort);
}
