package com.example.asm.service;

import com.example.asm.entity.*;
import com.example.asm.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class InboundService {

    @Autowired private DonNhapRepository donNhapRepo;
    @Autowired private ChiTietNhapRepository chiTietRepo;
    @Autowired private BienTheMoHinhRepository bienTheRepo;

    public List<DonNhap> findAll() {
        return donNhapRepo.findAll(Sort.by(Sort.Direction.DESC, "ngayNhap"));
    }

    public List<DonNhap> findByDateRange(LocalDate from, LocalDate to) {
        LocalDateTime start = (from != null)
                ? from.atStartOfDay()
                : LocalDate.of(2000, 1, 1).atStartOfDay(); // tránh underflow datetime2
        LocalDateTime end = (to != null)
                ? to.plusDays(1).atStartOfDay().minusNanos(1)
                : LocalDateTime.now().with(LocalTime.MAX);
        return donNhapRepo.findByNgayNhapBetween(start, end, Sort.by(Sort.Direction.DESC, "ngayNhap"));
    }

    public DonNhap findById(Integer id) {
        return donNhapRepo.findById(id).orElse(null);
    }

    public List<ChiTietNhap> findDetails(Integer maDonNhap) {
        return chiTietRepo.findByDonNhap_MaDonNhap(maDonNhap);
    }

    @Transactional
    public DonNhap create(TaiKhoan user, String ghiChu) {
        DonNhap dn = DonNhap.builder()
                .taiKhoan(user)
                .ngayNhap(LocalDateTime.now())
                .tongTienNhap(0L)
                .ghiChu(ghiChu)
                .build();
        return donNhapRepo.save(dn);
    }

    @Transactional
    public DonNhap update(Integer id, String ghiChu) {
        DonNhap dn = donNhapRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nhập"));
        dn.setGhiChu(ghiChu);
        return donNhapRepo.save(dn);
    }

    @Transactional
    public void deleteOrder(Integer id) {
        DonNhap dn = donNhapRepo.findById(id).orElse(null);
        if (dn == null) return;

        List<ChiTietNhap> details = chiTietRepo.findByDonNhap_MaDonNhap(id);
        for (ChiTietNhap ct : details) {
            BienTheMoHinh bt = ct.getBienThe();
            bt.setSoLuongTon(Math.max(0, bt.getSoLuongTon() - ct.getSoLuongNhap()));
            bienTheRepo.save(bt);
        }
        chiTietRepo.deleteAll(details);
        donNhapRepo.delete(dn);
    }

    @Transactional
    public ChiTietNhap addDetail(Integer maDonNhap, Integer maBienThe, int soLuong, long giaNhap) {
        DonNhap dn = donNhapRepo.findById(maDonNhap)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nhập"));
        BienTheMoHinh bt = bienTheRepo.findById(maBienThe)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng tồn kho"));

        bt.setSoLuongTon(bt.getSoLuongTon() + soLuong);
        bienTheRepo.save(bt);

        ChiTietNhap ct = ChiTietNhap.builder()
                .donNhap(dn)
                .bienThe(bt)
                .soLuongNhap(soLuong)
                .giaNhap(giaNhap)
                .build();
        ChiTietNhap saved = chiTietRepo.save(ct);
        refreshTongTien(dn);
        return saved;
    }

    @Transactional
    public ChiTietNhap updateDetail(Integer ctId, int soLuong, long giaNhap) {
        ChiTietNhap ct = chiTietRepo.findById(ctId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết"));
        BienTheMoHinh bt = ct.getBienThe();
        int diff = soLuong - ct.getSoLuongNhap();
        bt.setSoLuongTon(bt.getSoLuongTon() + diff);
        if (bt.getSoLuongTon() < 0) throw new IllegalStateException("Tồn kho âm");
        bienTheRepo.save(bt);

        ct.setSoLuongNhap(soLuong);
        ct.setGiaNhap(giaNhap);
        ChiTietNhap saved = chiTietRepo.save(ct);
        refreshTongTien(ct.getDonNhap());
        return saved;
    }

    @Transactional
    public void deleteDetail(Integer ctId) {
        ChiTietNhap ct = chiTietRepo.findById(ctId).orElse(null);
        if (ct == null) return;
        BienTheMoHinh bt = ct.getBienThe();
        bt.setSoLuongTon(Math.max(0, bt.getSoLuongTon() - ct.getSoLuongNhap()));
        bienTheRepo.save(bt);

        DonNhap dn = ct.getDonNhap();
        chiTietRepo.delete(ct);
        refreshTongTien(dn);
    }

    private void refreshTongTien(DonNhap dn) {
        long sum = chiTietRepo.findByDonNhap_MaDonNhap(dn.getMaDonNhap())
                .stream()
                .mapToLong(i -> i.getGiaNhap() * i.getSoLuongNhap())
                .sum();
        dn.setTongTienNhap(sum);
        donNhapRepo.save(dn);
    }
}
