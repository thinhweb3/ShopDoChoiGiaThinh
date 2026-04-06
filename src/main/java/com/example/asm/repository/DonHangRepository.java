package com.example.asm.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.asm.entity.DonHang;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    List<DonHang> findByTaiKhoan_MaTaiKhoan(Integer maTaiKhoan);
    List<DonHang> findByTrangThai(String trangThai);
    Page<DonHang> findByTrangThai(String trangThai, Pageable pageable);
    Page<DonHang> findByTrangThaiIn(Collection<String> trangThais, Pageable pageable);
    List<DonHang> findByNgayDatBetween(LocalDateTime from, LocalDateTime to);
    List<DonHang> findByTrangThaiThanhToan(String trangThaiThanhToan);
    long countByTrangThai(String trangThai);
    long countByTrangThaiIn(Collection<String> trangThais);
    List<DonHang> findTop5ByOrderByNgayDatDesc();

    // Phân trang và tìm kiếm
    Page<DonHang> findAll(Pageable pageable);

    @Query("""
           SELECT dh
           FROM DonHang dh
           LEFT JOIN dh.taiKhoan tk
           WHERE LOWER(COALESCE(tk.hoTen, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(COALESCE(tk.soDienThoai, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(COALESCE(dh.tenNguoiNhan, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(COALESCE(dh.soDienThoaiNhan, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(COALESCE(dh.emailNguoiNhan, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR CAST(dh.maDonHang AS string) LIKE CONCAT('%', :keyword, '%')
              OR LOWER(COALESCE(dh.diaChiGiaoHang, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    Page<DonHang> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
           SELECT dh
           FROM DonHang dh
           LEFT JOIN dh.taiKhoan tk
           WHERE (
                   LOWER(COALESCE(tk.hoTen, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(tk.soDienThoai, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(dh.tenNguoiNhan, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(dh.soDienThoaiNhan, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(dh.emailNguoiNhan, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR CAST(dh.maDonHang AS string) LIKE CONCAT('%', :keyword, '%')
                OR LOWER(COALESCE(dh.diaChiGiaoHang, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
           )
           AND dh.trangThai IN :trangThais
           """)
    Page<DonHang> searchByKeywordAndStatuses(@Param("keyword") String keyword,
                                             @Param("trangThais") Collection<String> trangThais,
                                             Pageable pageable);

    @Query(value = "SELECT EXTRACT(MONTH FROM NgayDat) as Thang, SUM(TongTien) as DoanhThu " +
                   "FROM DonHang " +
                   "WHERE EXTRACT(YEAR FROM NgayDat) = :year AND TrangThai = 'Hoàn thành' " +
                   "GROUP BY EXTRACT(MONTH FROM NgayDat) " +
                   "ORDER BY EXTRACT(MONTH FROM NgayDat)", nativeQuery = true)
    List<Object[]> getMonthlyRevenue(@Param("year") int year);
}
