package com.example.asm.controller;

import com.example.asm.entity.MoHinh;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.YeuThich;
import com.example.asm.repository.MoHinhRepository;
import com.example.asm.repository.YeuThichRepository;
import com.example.asm.service.AuthService;
import com.example.asm.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // <--- Import này

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired YeuThichRepository yeuThichRepo;
    @Autowired MoHinhRepository moHinhRepo;
    @Autowired AuthService authService;
    @Autowired CategoryService categoryService;

    @ModelAttribute("cates")
    public Object getCategories() { return categoryService.findAll(); }

    @GetMapping("/view")
    public String view(Model model) {
        if (!authService.isLogin()) return "redirect:/auth/login";
        TaiKhoan user = authService.getUser();
        model.addAttribute("favorites", yeuThichRepo.findByTaiKhoan_MaTaiKhoan(user.getMaTaiKhoan()));
        return "fragments/favorite"; 
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable("id") String maMoHinh, 
                         @RequestHeader(value = "referer", required = false) String referer,
                         RedirectAttributes params) {
        
        if (!authService.isLogin()) {
            return "redirect:/auth/login";
        }

        TaiKhoan user = authService.getUser();
        Optional<YeuThich> existItem = yeuThichRepo.findByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(user.getMaTaiKhoan(), maMoHinh);

        if (existItem.isPresent()) {
            yeuThichRepo.delete(existItem.get());
            params.addFlashAttribute("message", "Đã xóa khỏi danh sách yêu thích!");
            params.addFlashAttribute("type", "warning");
        } else {
            MoHinh moHinh = moHinhRepo.findById(maMoHinh).orElse(null);
            if (moHinh != null) {
                YeuThich item = YeuThich.builder()
                        .taiKhoan(user)
                        .moHinh(moHinh)
                        .ngayThem(LocalDateTime.now())
                        .build();
                yeuThichRepo.save(item);
                params.addFlashAttribute("message", "Đã thêm vào danh sách yêu thích!");
                params.addFlashAttribute("type", "success");
            }
        }

        return "redirect:" + (referer != null ? referer : "/home/index");
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") String maMoHinh, RedirectAttributes params) {
        if (!authService.isLogin()) return "redirect:/auth/login";
        
        TaiKhoan user = authService.getUser();
        Optional<YeuThich> item = yeuThichRepo.findByTaiKhoan_MaTaiKhoanAndMoHinh_MaMoHinh(user.getMaTaiKhoan(), maMoHinh);
        
        if(item.isPresent()){
            yeuThichRepo.delete(item.get());
            params.addFlashAttribute("message", "Đã xóa đồ chơi!");
            params.addFlashAttribute("type", "success");
        }
        
        return "redirect:/favorite/view";
    }
}
