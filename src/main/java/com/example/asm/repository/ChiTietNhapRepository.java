package com.example.asm.repository;

import com.example.asm.entity.ChiTietNhap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChiTietNhapRepository extends JpaRepository<ChiTietNhap, Integer> {
    List<ChiTietNhap> findByDonNhap_MaDonNhap(Integer maDonNhap);
    List<ChiTietNhap> findByBienThe_MaBienThe(Integer maBienThe);
}
