package com.example.asm.controller.admin;

import com.example.asm.entity.HangSanXuat;
import com.example.asm.repository.HangSanXuatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/manufacturers")
@PreAuthorize("@adminPermissionGuard.canAccessManufacturerSection()")
public class AdminManufacturerController {

    @Autowired
    private HangSanXuatRepository hangRepo;

    @GetMapping
    public String index(Model model) {
        List<HangSanXuat> list = hangRepo.findAll();
        model.addAttribute("manufacturers", list);
        return "admin/admin-manufacturer";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<HangSanXuat> getApi(@PathVariable Integer id) {
        return hangRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.canSaveManufacturer(#hang)")
    public String save(@ModelAttribute HangSanXuat hang, RedirectAttributes params) {
        try {
            if (hang.getMaHang() == null) {

                if (hangRepo.existsByTenHang(hang.getTenHang())) {
                    params.addFlashAttribute("message", "Tên hãng này đã tồn tại!");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/manufacturer";
                }
                params.addFlashAttribute("message", "Thêm hãng thành công!");
            } else {

                HangSanXuat oldHang = hangRepo.findById(hang.getMaHang()).orElse(null);
                if (oldHang != null && !oldHang.getTenHang().equals(hang.getTenHang())) {
                    if (hangRepo.existsByTenHang(hang.getTenHang())) {
                        params.addFlashAttribute("message", "Tên hãng mới bị trùng!");
                        params.addFlashAttribute("messageType", "error");
                        return "redirect:/admin/manufacturers";
                    }
                }
                params.addFlashAttribute("message", "Cập nhật thành công!");
            }
            
            params.addFlashAttribute("messageType", "success");
            hangRepo.save(hang);

        } catch (Exception e) {
            e.printStackTrace();
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/manufacturers";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('MANUFACTURER_DELETE')")
    public String delete(@PathVariable Integer id, RedirectAttributes params) {
        try {
            hangRepo.deleteById(id);
            params.addFlashAttribute("message", "Xóa hãng thành công!");
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Không thể xóa! Hãng này đang có đồ chơi.");
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/manufacturers";
    }
}
