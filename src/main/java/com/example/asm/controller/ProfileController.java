package com.example.asm.controller;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.service.AccountService;
import com.example.asm.service.AuthService;
import com.example.asm.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/account")
public class ProfileController {

    @Autowired AccountService accountService;
    @Autowired AuthService authService;
    @Autowired CategoryService categoryService;

    @ModelAttribute("cates")
    public Object getCategories() { 
        return categoryService.findAll(); 
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        if (!authService.isLogin()) {
            return "redirect:/auth/login";
        }

        TaiKhoan currentUser = authService.getUser();
        TaiKhoan userProfile = accountService.findById(currentUser.getMaTaiKhoan());
        
        model.addAttribute("user", userProfile);
        return "fragments/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(Model model, 
                                @ModelAttribute("user") TaiKhoan formUser) {
        TaiKhoan currentUser = authService.getUser();
        TaiKhoan userInDb = accountService.findById(currentUser.getMaTaiKhoan());

        if (!userInDb.getTenDangNhap().equals(formUser.getTenDangNhap())) {
            if (accountService.findByUsername(formUser.getTenDangNhap()) != null) {
                model.addAttribute("message", "Tên đăng nhập này đã có người dùng!");
                return "fragments/profile";
            }
        }

        if (!userInDb.getEmail().equals(formUser.getEmail())) {
            if (accountService.findByEmail(formUser.getEmail()) != null) {
                model.addAttribute("message", "Email này đã được sử dụng!");
                return "fragments/profile";
            }
        }

        try {
            userInDb.setHoTen(formUser.getHoTen());
            userInDb.setTenDangNhap(formUser.getTenDangNhap());
            userInDb.setEmail(formUser.getEmail());
            userInDb.setSoDienThoai(formUser.getSoDienThoai());
            userInDb.setDiaChi(formUser.getDiaChi());

            accountService.save(userInDb);
            authService.refreshAuthentication(userInDb);
            
            model.addAttribute("message", "Cập nhật thông tin thành công!");
            model.addAttribute("alertClass", "alert-success");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi: " + e.getMessage());
            model.addAttribute("alertClass", "alert-danger");
        }
        
        model.addAttribute("user", userInDb); 
        return "fragments/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(Model model,
                                 @RequestParam("currentPass") String currentPass,
                                 @RequestParam("newPass") String newPass,
                                 @RequestParam("confirmPass") String confirmPass) {
        
        TaiKhoan currentUser = authService.getUser();
        TaiKhoan userInDb = accountService.findById(currentUser.getMaTaiKhoan());

        if (!accountService.matchesPassword(currentPass, userInDb.getMatKhau())) {
            model.addAttribute("messPass", "Mật khẩu hiện tại không đúng!");
            model.addAttribute("user", userInDb);
            return "fragments/profile";
        }

        if (!newPass.equals(confirmPass)) {
            model.addAttribute("messPass", "Xác nhận mật khẩu mới không khớp!");
            model.addAttribute("user", userInDb);
            return "fragments/profile";
        }

        try {
            userInDb.setMatKhau(accountService.encodePassword(newPass));
            accountService.save(userInDb);
            authService.refreshAuthentication(userInDb);

            model.addAttribute("messPass", "Đổi mật khẩu thành công!");
            model.addAttribute("alertPassClass", "alert-success");
        } catch (Exception e) {
            model.addAttribute("messPass", "Lỗi đổi mật khẩu: " + e.getMessage());
        }

        model.addAttribute("user", userInDb);
        return "fragments/profile";
    }
}
