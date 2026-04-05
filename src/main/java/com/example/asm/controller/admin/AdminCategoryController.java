package com.example.asm.controller.admin;

import com.example.asm.entity.DanhMuc;
import com.example.asm.repository.DanhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("@adminPermissionGuard.canAccessCategorySection()")
public class AdminCategoryController {

    @Autowired private DanhMucRepository danhMucRepo;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("categories", danhMucRepo.findAll());
        return "admin/admin-category";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApi(@PathVariable Integer id) {
        return danhMucRepo.findById(id)
                .map(category -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("maDanhMuc", category.getMaDanhMuc());
                    payload.put("tenDanhMuc", category.getTenDanhMuc());
                    payload.put("moTa", category.getMoTa());
                    return ResponseEntity.ok(payload);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.canSaveCategory(#dm)")
    public String save(@ModelAttribute DanhMuc dm, RedirectAttributes params) {
        try {
            if (dm.getTenDanhMuc() != null) {
                dm.setTenDanhMuc(dm.getTenDanhMuc().trim());
            }

            if (dm.getMaDanhMuc() == null) {
                if (danhMucRepo.existsByTenDanhMuc(dm.getTenDanhMuc())) {
                    params.addFlashAttribute("message", "Tên danh mục đã tồn tại!");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/categories";
                }
                params.addFlashAttribute("message", "Thêm mới thành công!");
            } else {
                DanhMuc oldCategory = danhMucRepo.findById(dm.getMaDanhMuc()).orElse(null);
                if (oldCategory != null
                        && oldCategory.getTenDanhMuc() != null
                        && !oldCategory.getTenDanhMuc().equalsIgnoreCase(dm.getTenDanhMuc())
                        && danhMucRepo.existsByTenDanhMuc(dm.getTenDanhMuc())) {
                    params.addFlashAttribute("message", "Tên danh mục mới đã tồn tại!");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/categories";
                }
                params.addFlashAttribute("message", "Cập nhật thành công!");
            }
            params.addFlashAttribute("messageType", "success");
            danhMucRepo.save(dm);
        } catch (Exception e) {
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('CATEGORY_DELETE')")
    public String delete(@PathVariable Integer id, RedirectAttributes params) {
        try {
            danhMucRepo.deleteById(id);
            params.addFlashAttribute("message", "Xóa thành công!");
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Không thể xóa danh mục đang có đồ chơi!");
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/categories";
    }
}
