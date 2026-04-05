package com.example.asm.service;

import com.example.asm.dto.ThongKeDanhMucDTO;
import com.example.asm.dto.ThongKeKhachHangVIPDTO;
import com.example.asm.dto.ThongKeSanPhamBanChayDTO;
import com.example.asm.dto.ThongKeTongQuanDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    @PersistenceContext
    private EntityManager em;

    public List<ThongKeDanhMucDTO> thongKeDoanhThuTheoDanhMuc(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT
                dm.TenDanhMuc,
                COALESCE(SUM(ct.DonGia * ct.SoLuong), 0) AS TongDoanhThu,
                COALESCE(SUM(ct.SoLuong), 0) AS TongSoLuong,
                COALESCE(MAX(ct.DonGia), 0) AS GiaCaoNhat,
                COALESCE(MIN(ct.DonGia), 0) AS GiaThapNhat,
                COALESCE(AVG(ct.DonGia), 0) AS GiaTrungBinh
            FROM DonHang o
            JOIN ChiTietDonHang ct ON ct.MaDonHang = o.MaDonHang
            JOIN BienTheDoChoi bt ON bt.MaBienTheDoChoi = ct.MaBienTheDoChoi
            JOIN DoChoi mh ON mh.MaDoChoi = bt.MaDoChoi
            JOIN DanhMuc dm ON dm.MaDanhMuc = mh.MaDanhMuc
            WHERE o.NgayDat BETWEEN :startDate AND :endDate
            GROUP BY dm.TenDanhMuc
            ORDER BY TongDoanhThu DESC
            """;

        List<Object[]> rows = createNativeQuery(sql, startDate, endDate);
        List<ThongKeDanhMucDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ThongKeDanhMucDTO(
                toText(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toLong(row[5])
            ));
        }
        return result;
    }

    public List<ThongKeKhachHangVIPDTO> thongKeKhachHangVIP(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT
                ROW_NUMBER() OVER(ORDER BY COALESCE(SUM(ct.DonGia * ct.SoLuong), 0) DESC) AS STT,
                tk.HoTen,
                tk.Email,
                COUNT(DISTINCT o.MaDonHang) AS TongDonHang,
                COALESCE(SUM(ct.DonGia * ct.SoLuong), 0) AS TongChiTieu,
                CASE
                    WHEN COUNT(DISTINCT o.MaDonHang) > 0
                    THEN COALESCE(SUM(ct.DonGia * ct.SoLuong), 0) * 1.0 / COUNT(DISTINCT o.MaDonHang)
                    ELSE 0
                END AS GiaTriTrungBinh
            FROM TaiKhoan tk
            JOIN DonHang o ON o.MaTaiKhoan = tk.MaTaiKhoan
            LEFT JOIN ChiTietDonHang ct ON ct.MaDonHang = o.MaDonHang
            WHERE o.NgayDat BETWEEN :startDate AND :endDate
            GROUP BY tk.HoTen, tk.Email
            ORDER BY TongChiTieu DESC
            """;

        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("startDate", toSqlTimestamp(startDate))
            .setParameter("endDate", toSqlTimestamp(endDate))
            .setMaxResults(10)
            .getResultList();

        List<ThongKeKhachHangVIPDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ThongKeKhachHangVIPDTO(
                toInt(row[0]),
                toText(row[1]),
                toText(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toDouble(row[5])
            ));
        }
        return result;
    }

    public List<ThongKeSanPhamBanChayDTO> thongKeSanPhamBanChay(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        String sql = """
            SELECT
                mh.MaDoChoi AS MaSanPham,
                mh.TenDoChoi,
                dm.TenDanhMuc,
                COALESCE(SUM(ct.SoLuong), 0) AS TongSoLuong,
                COALESCE(SUM(ct.DonGia * ct.SoLuong), 0) AS TongDoanhThu,
                COALESCE(AVG(ct.DonGia), 0) AS GiaTrungBinh
            FROM DonHang o
            JOIN ChiTietDonHang ct ON ct.MaDonHang = o.MaDonHang
            JOIN BienTheDoChoi bt ON bt.MaBienTheDoChoi = ct.MaBienTheDoChoi
            JOIN DoChoi mh ON mh.MaDoChoi = bt.MaDoChoi
            JOIN DanhMuc dm ON dm.MaDanhMuc = mh.MaDanhMuc
            WHERE o.NgayDat BETWEEN :startDate AND :endDate
            GROUP BY mh.MaDoChoi, mh.TenDoChoi, dm.TenDanhMuc
            ORDER BY TongSoLuong DESC, TongDoanhThu DESC
            """;

        List<Object[]> rows = em.createNativeQuery(sql)
            .setParameter("startDate", toSqlTimestamp(startDate))
            .setParameter("endDate", toSqlTimestamp(endDate))
            .setMaxResults(limit)
            .getResultList();

        List<ThongKeSanPhamBanChayDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ThongKeSanPhamBanChayDTO(
                toText(row[0]),
                toText(row[1]),
                toText(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toDouble(row[5])
            ));
        }
        return result;
    }

    public ThongKeTongQuanDTO thongKeTongQuan(LocalDateTime startDate, LocalDateTime endDate) {
        String doanhThuSql = """
            SELECT COALESCE(SUM(ct.DonGia * ct.SoLuong), 0)
            FROM DonHang o
            LEFT JOIN ChiTietDonHang ct ON ct.MaDonHang = o.MaDonHang
            WHERE o.NgayDat BETWEEN :startDate AND :endDate
            """;
        Long tongDoanhThu = toLong(em.createNativeQuery(doanhThuSql)
            .setParameter("startDate", toSqlTimestamp(startDate))
            .setParameter("endDate", toSqlTimestamp(endDate))
            .getSingleResult());

        String tongDonHangSql = """
            SELECT COALESCE(COUNT(*), 0)
            FROM DonHang o
            WHERE o.NgayDat BETWEEN :startDate AND :endDate
            """;
        Long tongDonHang = toLong(em.createNativeQuery(tongDonHangSql)
            .setParameter("startDate", toSqlTimestamp(startDate))
            .setParameter("endDate", toSqlTimestamp(endDate))
            .getSingleResult());

        String tongSanPhamSql = """
            SELECT COALESCE(SUM(ct.SoLuong), 0)
            FROM DonHang o
            JOIN ChiTietDonHang ct ON ct.MaDonHang = o.MaDonHang
            WHERE o.NgayDat BETWEEN :startDate AND :endDate
            """;
        Long tongSanPhamBan = toLong(em.createNativeQuery(tongSanPhamSql)
            .setParameter("startDate", toSqlTimestamp(startDate))
            .setParameter("endDate", toSqlTimestamp(endDate))
            .getSingleResult());

        String tongKhachHangSql = """
            SELECT COALESCE(COUNT(DISTINCT o.MaTaiKhoan), 0)
            FROM DonHang o
            WHERE o.NgayDat BETWEEN :startDate AND :endDate
            """;
        Long tongKhachHang = toLong(em.createNativeQuery(tongKhachHangSql)
            .setParameter("startDate", toSqlTimestamp(startDate))
            .setParameter("endDate", toSqlTimestamp(endDate))
            .getSingleResult());

        LocalDate start = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();
        long daySpan = ChronoUnit.DAYS.between(start, end) + 1;
        LocalDate prevEnd = start.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(Math.max(daySpan - 1, 0));

        Long doanhThuKyTruoc = toLong(em.createNativeQuery(doanhThuSql)
            .setParameter("startDate", toSqlTimestamp(prevStart.atStartOfDay()))
            .setParameter("endDate", toSqlTimestamp(prevEnd.atTime(23, 59, 59)))
            .getSingleResult());

        double tyLeTangTruong = 0.0;
        if (doanhThuKyTruoc > 0) {
            tyLeTangTruong = ((tongDoanhThu - doanhThuKyTruoc) * 100.0) / doanhThuKyTruoc;
        }

        return new ThongKeTongQuanDTO(
            tongDoanhThu,
            tongDonHang,
            tongSanPhamBan,
            tongKhachHang,
            Math.round(tyLeTangTruong * 100.0) / 100.0,
            0L,
            0L,
            0L
        );
    }

    public List<Object[]> thongKeDoanhThuTheoThang(int year) {
        String sql = """
            SELECT
                EXTRACT(MONTH FROM o.NgayDat) AS Thang,
                COALESCE(SUM(ct.DonGia * ct.SoLuong), 0) AS DoanhThu,
                COUNT(DISTINCT o.MaDonHang) AS SoDon
            FROM DonHang o
            LEFT JOIN ChiTietDonHang ct ON ct.MaDonHang = o.MaDonHang
            WHERE EXTRACT(YEAR FROM o.NgayDat) = :year
            GROUP BY EXTRACT(MONTH FROM o.NgayDat)
            ORDER BY EXTRACT(MONTH FROM o.NgayDat)
            """;

        return em.createNativeQuery(sql)
            .setParameter("year", year)
            .getResultList();
    }

    public List<Object[]> thongKeDonHangTheoTrangThai() {
        String sql = """
            SELECT o.TrangThai, COUNT(*) AS SoLuong
            FROM DonHang o
            GROUP BY o.TrangThai
            """;
        return em.createNativeQuery(sql).getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> createNativeQuery(String sql, LocalDateTime startDate, LocalDateTime endDate) {
        return em.createNativeQuery(sql)
            .setParameter("startDate", toSqlTimestamp(startDate))
            .setParameter("endDate", toSqlTimestamp(endDate))
            .getResultList();
    }

    private Timestamp toSqlTimestamp(LocalDateTime value) {
        return Timestamp.valueOf(value);
    }

    private String toText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private Integer toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private Double toDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }
}
