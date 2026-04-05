package com.example.asm.repository;

import com.example.asm.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    List<DanhGia> findByMoHinh_MaMoHinh(String maMoHinh);
    List<DanhGia> findByTaiKhoan_MaTaiKhoan(Integer maTaiKhoan);
    List<DanhGia> findByMoHinh_MaMoHinhAndTrangThai(String maMoHinh, Boolean trangThai);
    List<DanhGia> findByMoHinh_MaMoHinhAndTrangThaiOrderByNgayDanhGiaDesc(String maMoHinh, Boolean trangThai);

}
