package com.example.asm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.ChiTietDonHang;
import com.example.asm.entity.DonHang;
import com.example.asm.entity.GioHang;
import com.example.asm.entity.KhuyenMai;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.ChiTietDonHangRepository;
import com.example.asm.repository.DonHangRepository;
import com.example.asm.repository.GioHangRepository;
import com.example.asm.repository.KhuyenMaiRepository;
import org.springframework.util.StringUtils;

@Service
public class OrderService {

    @Autowired DonHangRepository donHangRepo;
    @Autowired ChiTietDonHangRepository chiTietRepo;
    @Autowired GioHangRepository gioHangRepo;
    @Autowired BienTheMoHinhRepository bienTheRepo;
    @Autowired KhuyenMaiRepository kmRepo;
    @Autowired CartService cartService;
    @Value("${order.shipping-fee:30000}") long shippingFee;
    
    
    public DonHang create(DonHang donHang) {
        return donHangRepo.save(donHang);
    }

    public DonHang findById(Integer id) {
        return donHangRepo.findById(id).orElse(null);
    }

    public List<DonHang> findByUserId(Integer maTaiKhoan) {
        return donHangRepo.findByTaiKhoan_MaTaiKhoan(maTaiKhoan);
    }

    @Transactional(rollbackFor = Exception.class)
    public DonHang placeOrder(TaiKhoan user, String diaChi, String ghiChu, KhuyenMai voucher) {
        return placeOrder(
                user,
                diaChi,
                ghiChu,
                voucher,
                user != null ? user.getHoTen() : null,
                user != null ? user.getSoDienThoai() : null,
                user != null ? user.getEmail() : null
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public DonHang placeOrder(TaiKhoan user,
                              String diaChi,
                              String ghiChu,
                              KhuyenMai voucher,
                              String tenNguoiNhan,
                              String soDienThoaiNhan,
                              String emailNguoiNhan) {

        List<GioHang> cartItems = cartService != null
                ? cartService.getCart(user)
                : gioHangRepo.findByTaiKhoan_MaTaiKhoan(user.getMaTaiKhoan());
        if (cartItems.isEmpty()) throw new RuntimeException("Giỏ hàng trống!");

        long tongTienHang = cartItems.stream()
                .mapToLong(item -> item.getBienThe().getGiaBan() * item.getSoLuong())
                .sum();
        long phiVanChuyen = shippingFee;

        long discount = 0;
        if (voucher != null) {

            KhuyenMai kmDB = kmRepo.findById(voucher.getMaCode()).orElse(null);
            
            if (kmDB != null && kmDB.getTrangThai() && kmDB.getSoLuong() > 0 
                && LocalDateTime.now().isAfter(kmDB.getNgayBatDau()) 
                && LocalDateTime.now().isBefore(kmDB.getNgayKetThuc())
                && tongTienHang >= kmDB.getDonToiThieu()) {

                if (kmDB.getPhanTramGiam() != null && kmDB.getPhanTramGiam() > 0) {
                    discount = tongTienHang * kmDB.getPhanTramGiam() / 100;
                    if (kmDB.getGiamToiDa() != null && discount > kmDB.getGiamToiDa()) {
                        discount = kmDB.getGiamToiDa();
                    }
                } else if (kmDB.getSoTienGiam() != null) {
                    discount = kmDB.getSoTienGiam();
                }

                kmDB.setSoLuong(kmDB.getSoLuong() - 1);
                kmRepo.save(kmDB);
                
                voucher = kmDB;
            } else {
                voucher = null;
            }
        }

        long tongCong = tongTienHang + phiVanChuyen - discount;
        if (tongCong < 0) tongCong = 0;

        DonHang dh = DonHang.builder()
                .taiKhoan(user)
                .ngayDat(LocalDateTime.now())
                .diaChiGiaoHang(diaChi)
                .tenNguoiNhan(resolveRecipientName(user, tenNguoiNhan))
                .soDienThoaiNhan(resolveRecipientPhone(user, soDienThoaiNhan))
                .emailNguoiNhan(resolveRecipientEmail(user, emailNguoiNhan))
                .ghiChu(StringUtils.hasText(ghiChu) ? ghiChu.trim() : null)
                .tongTienHang(tongTienHang)
                .phiVanChuyen(phiVanChuyen)
                .tienGiamGia(discount)
                .khuyenMai(voucher)
                .tongTien(tongCong)
                .trangThai("Chờ xử lý")
                .trangThaiThanhToan("Chờ thanh toán")
                .build();
        
        DonHang savedDH = donHangRepo.save(dh);

        for (GioHang item : cartItems) {
            BienTheMoHinh bt = item.getBienThe();

            if (bt.getSoLuongTon() < item.getSoLuong()) {
                throw new RuntimeException("Hết hàng: " + bt.getMoHinh().getTenMoHinh() + " (" + bt.getKichThuoc() + ")");
            }

            ChiTietDonHang chiTiet = ChiTietDonHang.builder()
                    .donHang(savedDH)
                    .bienThe(bt)
                    .soLuong(item.getSoLuong())
                    .donGia(bt.getGiaBan())
                    .build();
            chiTietRepo.save(chiTiet);
        }

        if (cartService != null) {
            cartService.clear(user);
        } else if (user != null) {
            gioHangRepo.deleteByTaiKhoan_MaTaiKhoan(user.getMaTaiKhoan());
        }
        return savedDH;
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(Integer maDonHang, Integer maTaiKhoan) {
        DonHang dh = donHangRepo.findById(maDonHang).orElse(null);
        if (dh == null || dh.getTaiKhoan() == null || !dh.getTaiKhoan().getMaTaiKhoan().equals(maTaiKhoan)) {
            throw new RuntimeException("Đơn hàng không tồn tại hoặc không thuộc tài khoản của bạn.");
        }
        confirmPayment(maDonHang);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(Integer maDonHang) {
        DonHang dh = donHangRepo.findById(maDonHang).orElse(null);
        if (dh == null) {
            throw new RuntimeException("Đơn hàng không tồn tại.");
        }
        if ("Đã thanh toán".equals(dh.getTrangThaiThanhToan())) {
            return; 
        }
        
        List<ChiTietDonHang> chiTiets = chiTietRepo.findByDonHang_MaDonHang(maDonHang);
        for (ChiTietDonHang ct : chiTiets) {
            BienTheMoHinh bt = ct.getBienThe();
            if (bt.getSoLuongTon() < ct.getSoLuong()) {
                throw new RuntimeException("Hết hàng: " + bt.getMoHinh().getTenMoHinh());
            }

            bt.setSoLuongTon(bt.getSoLuongTon() - ct.getSoLuong());
            bienTheRepo.save(bt);
        }
        dh.setTrangThaiThanhToan("Đã thanh toán");
        donHangRepo.save(dh);
    }

    private String resolveRecipientName(TaiKhoan user, String providedName) {
        if (StringUtils.hasText(providedName)) {
            return providedName.trim();
        }
        return user != null ? user.getHoTen() : null;
    }

    private String resolveRecipientPhone(TaiKhoan user, String providedPhone) {
        if (StringUtils.hasText(providedPhone)) {
            return providedPhone.trim();
        }
        return user != null ? user.getSoDienThoai() : null;
    }

    private String resolveRecipientEmail(TaiKhoan user, String providedEmail) {
        if (StringUtils.hasText(providedEmail)) {
            return providedEmail.trim();
        }
        return user != null ? user.getEmail() : null;
    }
}
