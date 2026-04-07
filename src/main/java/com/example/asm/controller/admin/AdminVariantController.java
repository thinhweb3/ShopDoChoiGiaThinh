package com.example.asm.controller.admin;

import com.example.asm.entity.BienTheMoHinh;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/variants")
@PreAuthorize("@adminPermissionGuard.canAccessProductSection()")
public class AdminVariantController {

    @GetMapping("/{maMoHinh}")
    public String index(@PathVariable String maMoHinh, RedirectAttributes params) {
        return redirectToProductStock(params);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> getApi(@PathVariable Integer id) {
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.canSaveVariant(#bt)")
    public String save(@ModelAttribute BienTheMoHinh bt, @RequestParam("maMoHinhStr") String maMoHinh, RedirectAttributes params) {
        return redirectToProductStock(params);
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('PRODUCT_VARIANT_MANAGE')")
    public String delete(@PathVariable Integer id, RedirectAttributes params) {
        return redirectToProductStock(params);
    }

    private String redirectToProductStock(RedirectAttributes params) {
        params.addFlashAttribute("message", "Đã gộp phần này vào tồn kho sản phẩm. Hãy sửa giá bán và tồn kho ở Quản lý đồ chơi.");
        params.addFlashAttribute("messageType", "warning");
        return "redirect:/admin/products";
    }
}
