package com.example.asm.controller.admin;

import com.example.asm.entity.DanhMuc;
import com.example.asm.entity.HangSanXuat;
import com.example.asm.entity.MoHinh;
import com.example.asm.repository.DanhMucRepository;
import com.example.asm.repository.HangSanXuatRepository;
import com.example.asm.repository.LoaiHangRepository;
import com.example.asm.repository.MoHinhRepository;
import com.example.asm.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("@adminPermissionGuard.canAccessProductSection()")
public class AdminProductController {

    @Autowired private MoHinhRepository moHinhRepo;
    @Autowired private DanhMucRepository danhMucRepo;
    @Autowired private HangSanXuatRepository hangRepo;
    @Autowired private LoaiHangRepository loaiHangRepo;
    @Autowired private FileService fileService;

    @GetMapping
    public String index(Model model) {
        DanhMuc defaultCategory = resolveCategory(null);
        HangSanXuat defaultManufacturer = resolveManufacturer(null);
        model.addAttribute("products", moHinhRepo.findAll());
        model.addAttribute("categories", danhMucRepo.findAll());
        model.addAttribute("productTypes", loaiHangRepo.findByTrangThaiTrueOrderByTenLoaiHangAsc());
        model.addAttribute("manufacturers", hangRepo.findAll());
        model.addAttribute("defaultCategory", defaultCategory);
        model.addAttribute("defaultManufacturer", defaultManufacturer);
        return "admin/admin-product";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApi(@PathVariable String id) {
        return moHinhRepo.findById(id)
                .map(product -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("maMoHinh", product.getMaMoHinh());
                    payload.put("tenMoHinh", product.getTenMoHinh());
                    payload.put("maDanhMuc", product.getDanhMuc() != null ? product.getDanhMuc().getMaDanhMuc() : null);
                    payload.put("maHang", product.getHangSanXuat() != null ? product.getHangSanXuat().getMaHang() : null);
                    payload.put("hinhAnh", product.getHinhAnh());
                    payload.put("tyLe", product.getTyLe());
                    payload.put("chatLieu", product.getChatLieu());
                    payload.put("nhomHang", product.getNhomHang());
                    payload.put("giaBan", product.getGiaBan());
                    payload.put("giaVon", product.getGiaVon());
                    payload.put("tonKho", product.getTonKho());
                    payload.put("tonKhoToiThieu", product.getTonKhoToiThieu());
                    payload.put("tonKhoToiDa", product.getTonKhoToiDa());
                    payload.put("donViTinh", product.getDonViTinh());
                    payload.put("quyCach", product.getQuyCach());
                    payload.put("dangKy", Boolean.TRUE.equals(product.getDangKy()));
                    payload.put("duocBan", Boolean.TRUE.equals(product.getDuocBan()));
                    payload.put("moTa", product.getMoTa());
                    payload.put("mauGhiChu", product.getMauGhiChu());
                    payload.put("trangThai", Boolean.TRUE.equals(product.getTrangThai()));
                    return ResponseEntity.ok(payload);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.canSaveProduct(#mh)")
    public String save(@ModelAttribute MoHinh mh,
                       @RequestParam(value = "maDanhMuc", required = false) String maDanhMuc,
                       @RequestParam(value = "maHang", required = false) String maHang,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       RedirectAttributes params) {
        try {
            if (!StringUtils.hasText(mh.getMaMoHinh()) || !StringUtils.hasText(mh.getTenMoHinh())) {
                params.addFlashAttribute("message", "Chi can nhap ma hang va ten hang.");
                params.addFlashAttribute("messageType", "error");
                return "redirect:/admin/products";
            }

            mh.setMaMoHinh(mh.getMaMoHinh().trim());
            mh.setTenMoHinh(mh.getTenMoHinh().trim());

            mh.setDanhMuc(resolveCategory(parseOptionalInteger(maDanhMuc)));

            mh.setHangSanXuat(resolveManufacturer(parseOptionalInteger(maHang)));
            applyDefaultValues(mh);

            MoHinh old = moHinhRepo.findById(mh.getMaMoHinh()).orElse(null);
            boolean isExist = old != null;
            
            if (!isExist) {
                mh.setCreatedAt(LocalDateTime.now());
                mh.setUpdatedAt(LocalDateTime.now());
                params.addFlashAttribute("message", "Thêm đồ chơi thành công!");
            } else {
                mh.setCreatedAt(old.getCreatedAt());
                mh.setUpdatedAt(LocalDateTime.now());
                params.addFlashAttribute("message", "Cập nhật thành công!");
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String savedFileName = fileService.save(imageFile, "images", mh.getMaMoHinh());
                if (savedFileName != null && !savedFileName.isBlank()) {
                    mh.setHinhAnh(savedFileName);
                }
            } else if (isExist) {
                mh.setHinhAnh(old.getHinhAnh());
            }

            params.addFlashAttribute("messageType", "success");
            moHinhRepo.save(mh);
        } catch (Exception e) {
            e.printStackTrace();
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/products";
    }

    private DanhMuc resolveCategory(Integer maDanhMuc) {
        if (maDanhMuc != null) {
            return danhMucRepo.findById(maDanhMuc)
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại."));
        }

        return danhMucRepo.findByTenDanhMuc("Chưa phân loại")
                .orElseGet(() -> danhMucRepo.save(DanhMuc.builder()
                        .tenDanhMuc("Chưa phân loại")
                        .moTa("Danh mục mặc định cho đồ chơi chưa gán loại hàng")
                        .build()));
    }

    private HangSanXuat resolveManufacturer(Integer maHang) {
        if (maHang != null) {
            return hangRepo.findById(maHang).orElse(null);
        }

        return hangRepo.findByTenHang("Không")
                .orElseGet(() -> hangRepo.save(HangSanXuat.builder()
                        .tenHang("Không")
                        .website("Không")
                        .moTa("Thương hiệu mặc định")
                        .build()));
    }

    private void applyDefaultValues(MoHinh mh) {
        mh.setHinhAnh(defaultText(mh.getHinhAnh(), "logo.jpg"));
        mh.setTyLe(defaultText(mh.getTyLe(), "Không"));
        mh.setChatLieu(defaultText(mh.getChatLieu(), "Không"));
        mh.setNhomHang(defaultText(mh.getNhomHang(), "Không"));
        mh.setGiaBan(defaultNumber(mh.getGiaBan()));
        mh.setGiaVon(defaultNumber(mh.getGiaVon()));
        mh.setTonKho(defaultInteger(mh.getTonKho()));
        mh.setTonKhoToiThieu(defaultInteger(mh.getTonKhoToiThieu()));
        mh.setTonKhoToiDa(defaultInteger(mh.getTonKhoToiDa()));
        mh.setDonViTinh(defaultText(mh.getDonViTinh(), "Không"));
        mh.setQuyCach(defaultText(mh.getQuyCach(), "Không"));
        mh.setDangKy(Boolean.TRUE.equals(mh.getDangKy()));
        mh.setDuocBan(mh.getDuocBan() == null ? true : mh.getDuocBan());
        mh.setMoTa(defaultText(mh.getMoTa(), "Không"));
        mh.setMauGhiChu(defaultText(mh.getMauGhiChu(), "Không"));
        mh.setTrangThai(mh.getTrangThai() == null ? true : mh.getTrangThai());
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private Long defaultNumber(Long value) {
        return value == null ? 0L : value;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer parseOptionalInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Integer.valueOf(value.trim());
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('PRODUCT_DELETE')")
    public String delete(@PathVariable String id, RedirectAttributes params) {
        try {
            moHinhRepo.deleteById(id);
            params.addFlashAttribute("message", "Xóa thành công!");
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Không thể xóa! Đồ chơi này đang có biến thể hoặc đơn hàng.");
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/products";
    }
}
