package com.example.asm.service;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.GioHang;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.GioHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired GioHangRepository gioHangRepo;
    @Autowired BienTheMoHinhRepository bienTheRepo;

    public List<GioHang> getCart(TaiKhoan user) {
        return gioHangRepo.findByTaiKhoan_MaTaiKhoan(user.getMaTaiKhoan());
    }

    public void add(TaiKhoan user, Integer maBienThe, int soLuong) {
        BienTheMoHinh bt = bienTheRepo.findById(maBienThe)
                .orElseThrow(() -> new RuntimeException("Đồ chơi không tồn tại"));

        if (bt.getSoLuongTon() < soLuong) {
            throw new RuntimeException("Kho không đủ hàng!");
        }

        Optional<GioHang> optItem = gioHangRepo.findByTaiKhoan_MaTaiKhoanAndBienThe_MaBienThe(user.getMaTaiKhoan(), maBienThe);

        GioHang item;
        if (optItem.isPresent()) {
            item = optItem.get();
            item.setSoLuong(item.getSoLuong() + soLuong);
            item.setNgayCapNhat(LocalDateTime.now());
        } else {
            item = GioHang.builder()
                    .taiKhoan(user)
                    .bienThe(bt)
                    .soLuong(soLuong)
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
        }
        gioHangRepo.save(item);
    }

    public void update(Integer maGioHang, int qty) {
        GioHang item = gioHangRepo.findById(maGioHang).orElse(null);
        if (item != null) {
            if (qty > 0) {
                item.setSoLuong(qty);
                item.setNgayCapNhat(LocalDateTime.now());
                gioHangRepo.save(item);
            } else {
                gioHangRepo.deleteById(maGioHang);
            }
        }
    }

    public void remove(Integer maGioHang) {
        gioHangRepo.deleteById(maGioHang);
    }

    @Transactional
    public void clear(TaiKhoan user) {
        gioHangRepo.deleteByTaiKhoan_MaTaiKhoan(user.getMaTaiKhoan());
    }

    public long getAmount(TaiKhoan user) {
        return getCart(user).stream()
               .mapToLong(i -> i.getSoLuong() * i.getBienThe().getGiaBan())
               .sum();
    }
    
    public int getCount(TaiKhoan user) {
        return getCart(user).size();
    }
}
