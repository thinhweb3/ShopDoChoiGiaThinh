package com.example.asm.controller.admin;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.MoHinh;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.MoHinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/variants")
@PreAuthorize("@adminPermissionGuard.canAccessProductSection()")
public class AdminVariantController {

    @Autowired private BienTheMoHinhRepository variantRepo;
    @Autowired private MoHinhRepository moHinhRepo;

    @GetMapping("/{maMoHinh}")
    public String index(@PathVariable String maMoHinh, Model model) {
        MoHinh product = moHinhRepo.findById(maMoHinh).orElse(null);
        if (product == null) return "redirect:/admin/products";

        List<BienTheMoHinh> list = variantRepo.findByMoHinh_MaMoHinh(maMoHinh);
        
        model.addAttribute("product", product);
        model.addAttribute("variants", list);
        return "admin/admin-variant";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<BienTheMoHinh> getApi(@PathVariable Integer id) {
        return variantRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.canSaveVariant(#bt)")
    public String save(@ModelAttribute BienTheMoHinh bt, @RequestParam("maMoHinhStr") String maMoHinh, RedirectAttributes params) {
        try {

            MoHinh parent = new MoHinh();
            parent.setMaMoHinh(maMoHinh);
            bt.setMoHinh(parent);

            if(bt.getSku() == null || bt.getSku().isEmpty()) {
                bt.setSku("SKU-" + maMoHinh + "-" + System.currentTimeMillis());
            }

            variantRepo.save(bt);
            params.addFlashAttribute("message", "Lưu biến thể thành công!");
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/variants/" + maMoHinh;
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('PRODUCT_VARIANT_MANAGE')")
    public String delete(@PathVariable Integer id, RedirectAttributes params) {
        String parentId = "";
        try {
            BienTheMoHinh bt = variantRepo.findById(id).orElse(null);
            if (bt != null) {
                parentId = bt.getMoHinh().getMaMoHinh();
                variantRepo.delete(bt);
                params.addFlashAttribute("message", "Xóa biến thể thành công!");
                params.addFlashAttribute("messageType", "success");
            }
        } catch (Exception e) {
            params.addFlashAttribute("message", "Không thể xóa! Dữ liệu đang được sử dụng.");
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/variants/" + parentId;
    }
}
