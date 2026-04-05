package com.example.asm.controller.admin;

import com.example.asm.entity.LoaiHang;
import com.example.asm.repository.LoaiHangRepository;
import com.example.asm.repository.MoHinhRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/types")
@PreAuthorize("@adminPermissionGuard.canAccessProductSection()")
public class AdminTypeController {

    private final LoaiHangRepository loaiHangRepository;
    private final MoHinhRepository moHinhRepository;

    public AdminTypeController(LoaiHangRepository loaiHangRepository, MoHinhRepository moHinhRepository) {
        this.loaiHangRepository = loaiHangRepository;
        this.moHinhRepository = moHinhRepository;
    }

    @GetMapping
    public String index(Model model) {
        Map<String, Long> typeUsage = new LinkedHashMap<>();
        for (LoaiHang type : loaiHangRepository.findAllByOrderByTenLoaiHangAsc()) {
            String typeName = normalize(type.getTenLoaiHang());
            if (typeName != null) {
                typeUsage.put(typeName, moHinhRepository.countByNhomHangIgnoreCase(typeName));
            }
        }
        model.addAttribute("types", loaiHangRepository.findAllByOrderByTenLoaiHangAsc());
        model.addAttribute("typeUsage", typeUsage);
        return "admin/admin-type";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApi(@PathVariable Integer id) {
        return loaiHangRepository.findById(id)
                .map(type -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("maLoaiHang", type.getMaLoaiHang());
                    payload.put("tenLoaiHang", type.getTenLoaiHang());
                    payload.put("moTa", type.getMoTa());
                    payload.put("trangThai", Boolean.TRUE.equals(type.getTrangThai()));
                    payload.put("soDoChoi", moHinhRepository.countByNhomHangIgnoreCase(type.getTenLoaiHang()));
                    return ResponseEntity.ok(payload);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.hasPermission('PRODUCT_CREATE') or @adminPermissionGuard.hasPermission('PRODUCT_UPDATE')")
    public String save(@ModelAttribute LoaiHang loaiHang, RedirectAttributes params) {
        try {
            String normalizedName = normalize(loaiHang.getTenLoaiHang());
            if (normalizedName == null) {
                params.addFlashAttribute("message", "Tên loại không được để trống.");
                params.addFlashAttribute("messageType", "error");
                return "redirect:/admin/types";
            }

            loaiHang.setTenLoaiHang(normalizedName);
            loaiHang.setTrangThai(Boolean.TRUE.equals(loaiHang.getTrangThai()));

            if (loaiHang.getMaLoaiHang() == null) {
                if (loaiHangRepository.existsByTenLoaiHangIgnoreCase(normalizedName)) {
                    params.addFlashAttribute("message", "Tên loại đã tồn tại!");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/types";
                }
                loaiHangRepository.save(loaiHang);
                params.addFlashAttribute("message", "Thêm loại thành công!");
                params.addFlashAttribute("messageType", "success");
                return "redirect:/admin/types";
            }

            LoaiHang existing = loaiHangRepository.findById(loaiHang.getMaLoaiHang()).orElse(null);
            if (existing == null) {
                params.addFlashAttribute("message", "Không tìm thấy loại cần cập nhật.");
                params.addFlashAttribute("messageType", "error");
                return "redirect:/admin/types";
            }

            String oldName = normalize(existing.getTenLoaiHang());
            if (oldName != null
                    && !oldName.equalsIgnoreCase(normalizedName)
                    && loaiHangRepository.existsByTenLoaiHangIgnoreCase(normalizedName)) {
                params.addFlashAttribute("message", "Tên loại mới đã tồn tại!");
                params.addFlashAttribute("messageType", "error");
                return "redirect:/admin/types";
            }

            existing.setTenLoaiHang(normalizedName);
            existing.setMoTa(loaiHang.getMoTa());
            existing.setTrangThai(Boolean.TRUE.equals(loaiHang.getTrangThai()));
            loaiHangRepository.save(existing);

            if (oldName != null && !oldName.equalsIgnoreCase(normalizedName)) {
                moHinhRepository.renameNhomHang(oldName, normalizedName);
            }

            params.addFlashAttribute("message", "Cập nhật loại thành công!");
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/types";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('PRODUCT_DELETE')")
    public String delete(@PathVariable Integer id, RedirectAttributes params) {
        LoaiHang type = loaiHangRepository.findById(id).orElse(null);
        if (type == null) {
            params.addFlashAttribute("message", "Không tìm thấy loại.");
            params.addFlashAttribute("messageType", "error");
            return "redirect:/admin/types";
        }

        long usageCount = moHinhRepository.countByNhomHangIgnoreCase(type.getTenLoaiHang());
        if (usageCount > 0) {
            params.addFlashAttribute("message", "Không thể xóa loại đang được gán cho đồ chơi.");
            params.addFlashAttribute("messageType", "error");
            return "redirect:/admin/types";
        }

        loaiHangRepository.deleteById(id);
        params.addFlashAttribute("message", "Xóa loại thành công!");
        params.addFlashAttribute("messageType", "success");
        return "redirect:/admin/types";
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
