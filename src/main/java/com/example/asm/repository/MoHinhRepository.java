package com.example.asm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.asm.entity.MoHinh;

public interface MoHinhRepository extends JpaRepository<MoHinh, String> {

    List<MoHinh> findByDanhMuc_MaDanhMuc(Integer maDanhMuc);
    
    List<MoHinh> findByHangSanXuat_MaHang(Integer maHang);
    
    List<MoHinh> findByTenMoHinhContainingIgnoreCase(String keyword);

    @Query("SELECT m FROM MoHinh m WHERE m.trangThai = :trangThai")
    List<MoHinh> findByTrangThai(@Param("trangThai") Boolean trangThai);

    @Query("SELECT m FROM MoHinh m " +
            "LEFT JOIN m.bienThes bt " +
            "LEFT JOIN bt.chiTietDonHangs ctdh " +
            "GROUP BY m " +
            "ORDER BY COUNT(ctdh) DESC")
     List<MoHinh> findTop8BestSelling(Pageable pageable);
    
    @Query("SELECT m FROM MoHinh m " +
            "LEFT JOIN m.bienThes bt " +
            "WHERE bt.giaBan >= 900000 " +
            "GROUP BY m ")
     List<MoHinh> findOn900(Pageable pageable);
    
    @Query("SELECT m FROM MoHinh m JOIN m.bienThes bt WHERE bt.maBienThe = :maBienThe AND bt.giaBan > :giaBan")
    Page<MoHinh> findByBienTheVaGia(@Param("maBienThe") Integer maBienThe, @Param("giaBan") Long giaBan, Pageable pageable);

    @Query("SELECT m FROM MoHinh m LEFT JOIN FETCH m.bienThes WHERE m.maMoHinh = :maMoHinh")
    Optional<MoHinh> findByIdWithBienThe(@Param("maMoHinh") String maMoHinh);

    @Query("""
            select count(m)
            from MoHinh m
            where m.nhomHang is not null
              and lower(m.nhomHang) = lower(:nhomHang)
            """)
    long countByNhomHangIgnoreCase(@Param("nhomHang") String nhomHang);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MoHinh m
            set m.nhomHang = :newName
            where m.nhomHang is not null
              and lower(m.nhomHang) = lower(:oldName)
            """)
    int renameNhomHang(@Param("oldName") String oldName, @Param("newName") String newName);
}
