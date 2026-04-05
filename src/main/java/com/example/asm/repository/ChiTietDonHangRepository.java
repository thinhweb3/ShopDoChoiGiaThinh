package com.example.asm.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.asm.entity.ChiTietDonHang;

public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Integer> {
    List<ChiTietDonHang> findByDonHang_MaDonHang(Integer maDonHang);
    List<ChiTietDonHang> findByBienThe_MaBienThe(Integer maBienThe);
    
    @Query("SELECT c.bienThe.moHinh, SUM(c.soLuong) FROM ChiTietDonHang c " +
           "JOIN c.donHang d " +
           "WHERE d.ngayDat >= :startDate " +
           "GROUP BY c.bienThe.moHinh " +
           "ORDER BY SUM(c.soLuong) DESC")
    List<Object[]> findTopSellingInLastDays(@Param("startDate") LocalDateTime startDate, PageRequest pageable);
}
