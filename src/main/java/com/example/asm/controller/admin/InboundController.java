package com.example.asm.controller.admin;

import com.example.asm.entity.ChiTietNhap;
import com.example.asm.entity.DonNhap;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.service.AuthService;
import com.example.asm.service.InboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/inbound")
@PreAuthorize("@adminPermissionGuard.canAccessInboundSection()")
public class InboundController {

    @Autowired private InboundService inboundService;
    @Autowired private BienTheMoHinhRepository bienTheRepo;
    @Autowired private AuthService authService;

    @GetMapping
    public String list(@RequestParam(name = "don", required = false) Integer selectedId,
                       @RequestParam(name = "from", required = false) String fromStr,
                       @RequestParam(name = "to", required = false) String toStr,
                       Model model) {

        LocalDate from = null, to = null;
        if (fromStr != null && !fromStr.isBlank()) from = LocalDate.parse(fromStr);
        if (toStr != null && !toStr.isBlank()) to = LocalDate.parse(toStr);

        List<DonNhap> orders = (from != null || to != null)
                ? inboundService.findByDateRange(from, to)
                : inboundService.findAll();

        DonNhap selected = null;
        if (selectedId != null) selected = inboundService.findById(selectedId);
        if (selected == null && !orders.isEmpty()) selected = orders.get(0);

        List<ChiTietNhap> details = (selected != null)
                ? inboundService.findDetails(selected.getMaDonNhap())
                : List.of();

        model.addAttribute("orders", orders);
        model.addAttribute("selected", selected);
        model.addAttribute("details", details);
        model.addAttribute("variants", bienTheRepo.findAll());
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/inbound";
    }

    @PostMapping("/create")
    @PreAuthorize("@adminPermissionGuard.hasPermission('INBOUND_CREATE')")
    public String create(@RequestParam(value = "ghiChu", required = false) String ghiChu,
                         RedirectAttributes ra) {
        TaiKhoan user = authService.getUser();
        if (user == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để tạo đơn nhập");
            return "redirect:/auth/login";
        }
        DonNhap dn = inboundService.create(user, ghiChu);
        ra.addFlashAttribute("success", "Đã tạo đơn nhập #" + dn.getMaDonNhap());
        return "redirect:/admin/inbound?don="+  dn.getMaDonNhap();
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("@adminPermissionGuard.hasPermission('INBOUND_UPDATE')")
    public String update(@PathVariable Integer id,
                         @RequestParam("ghiChu") String ghiChu,
                         RedirectAttributes ra) {
        inboundService.update(id, ghiChu);
        ra.addFlashAttribute("success", "Đã lưu ghi chú");
        return "redirect:/admin/inbound?don="+  id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("@adminPermissionGuard.hasPermission('INBOUND_DELETE')")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        inboundService.deleteOrder(id);
        ra.addFlashAttribute("success", "Đã xóa đơn nhập");
        return "redirect:/admin/inbound";
    }

    @PostMapping("/{id}/detail")
    @PreAuthorize("@adminPermissionGuard.hasPermission('INBOUND_UPDATE')")
    public String addDetail(@PathVariable Integer id,
                            @RequestParam("maBienThe") Integer maBienThe,
                            @RequestParam("soLuong") Integer soLuong,
                            @RequestParam("giaNhap") Long giaNhap,
                            RedirectAttributes ra) {
        inboundService.addDetail(id, maBienThe, soLuong, giaNhap);
        ra.addFlashAttribute("success", "Đã thêm chi tiết");
        return "redirect:/admin/inbound?don=" + id;
    }

    @PostMapping("/{donId}/detail/{detailId}/update")
    @PreAuthorize("@adminPermissionGuard.hasPermission('INBOUND_UPDATE')")
    public String updateDetail(@PathVariable Integer donId,
                               @PathVariable Integer detailId,
                               @RequestParam("soLuong") Integer soLuong,
                               @RequestParam("giaNhap") Long giaNhap,
                               RedirectAttributes ra) {
        inboundService.updateDetail(detailId, soLuong, giaNhap);
        ra.addFlashAttribute("success", "Đã cập nhật chi tiết");
        return "redirect:/admin/inbound?don=" + donId;
    }

    @PostMapping("/{donId}/detail/{detailId}/delete")
    @PreAuthorize("@adminPermissionGuard.hasPermission('INBOUND_DELETE')")
    public String deleteDetail(@PathVariable Integer donId,
                               @PathVariable Integer detailId,
                               RedirectAttributes ra) {
        inboundService.deleteDetail(detailId);
        ra.addFlashAttribute("success", "Đã xóa chi tiết");
        return "redirect:/admin/inbound?don=" + donId;
    }
}
