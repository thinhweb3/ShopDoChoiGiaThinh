package com.example.asm.service;

import com.example.asm.entity.DanhGia;
import com.example.asm.entity.MoHinh;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.DanhGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RatingService {
    @Autowired
    private DanhGiaRepository danhGiaRepo;

    public List<DanhGia> getProductReviews(String maMoHinh) {
        return danhGiaRepo.findByMoHinh_MaMoHinhAndTrangThaiOrderByNgayDanhGiaDesc(maMoHinh, true);
    }

    public void addRating(TaiKhoan user, String maMoHinh, Byte stars, String comment) {
        DanhGia dg = new DanhGia();
        dg.setTaiKhoan(user);
        
        MoHinh mh = new MoHinh();
        mh.setMaMoHinh(maMoHinh);
        dg.setMoHinh(mh);
        
        dg.setSoSao(stars);
        dg.setNhanXet(comment);
        dg.setNgayDanhGia(LocalDateTime.now());
        dg.setTrangThai(true);
        
        danhGiaRepo.save(dg);
    }
}