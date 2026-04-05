package com.example.asm.controller.admin;

import com.example.asm.entity.KhuyenMai;
import com.example.asm.repository.KhuyenMaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/promotions")
@PreAuthorize("@adminPermissionGuard.canAccessPromotionSection()")
public class AdminPromotionController {

    @Autowired
    private KhuyenMaiRepository kmRepo;

    @GetMapping
    public String index(Model model) {
        List<KhuyenMai> list = kmRepo.findAll();
        model.addAttribute("promotions", list);
        return "admin/admin-promotion";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<KhuyenMai> getApi(@PathVariable String id) {
        return kmRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.canSavePromotion(#km)")
    public String save(@ModelAttribute KhuyenMai km, RedirectAttributes params) {
        try {

            if (km.getNgayBatDau() != null && km.getNgayKetThuc() != null 
                && km.getNgayBatDau().isAfter(km.getNgayKetThuc())) {
                params.addFlashAttribute("message", "Ngày kết thúc phải sau ngày bắt đầu!");
                params.addFlashAttribute("messageType", "error");
                return "redirect:/admin/promotions";
            }

            if (!kmRepo.existsById(km.getMaCode())) {
                params.addFlashAttribute("message", "Tạo mã mới thành công!");
            } else {
                params.addFlashAttribute("message", "Cập nhật mã thành công!");
            }

            kmRepo.save(km);
            params.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            e.printStackTrace();
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/promotions";
    }

    // 4. Xóa mã
    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('PROMOTION_DELETE')")
    public String delete(@PathVariable String id, RedirectAttributes params) {
        try {
            kmRepo.deleteById(id);
            params.addFlashAttribute("message", "Xóa thành công!");
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Không thể xóa mã này (đã có đơn hàng sử dụng)!");
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/promotions";
    }
}
