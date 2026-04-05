package com.example.asm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.asm.entity.ChiTietDonHang;
import com.example.asm.repository.ChiTietDonHangRepository;

@Service
public class OrderDetailService {
    @Autowired ChiTietDonHangRepository chiTietRepo;

    public List<ChiTietDonHang> findByOrderId(Integer maDonHang) {
        return chiTietRepo.findByDonHang_MaDonHang(maDonHang);
    }

    public void save(ChiTietDonHang chiTiet) {
        chiTietRepo.save(chiTiet);
    }
}