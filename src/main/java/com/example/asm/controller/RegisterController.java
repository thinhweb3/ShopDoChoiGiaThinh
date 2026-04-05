package com.example.asm.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import com.example.asm.service.AccountService;
import com.example.asm.service.CategoryService;
import com.example.asm.service.MailService;
import com.example.asm.service.VaiTroService;

@Controller
@RequestMapping("/account")
public class RegisterController {

    @Autowired AccountService accountService;
    @Autowired CategoryService categoryService;
    @Autowired MailService mailService;
    @Autowired VaiTroService vaiTroService;

    @ModelAttribute("cates")
    public Object getCategories() { 
        return categoryService.findAll(); 
    }

    @GetMapping("/sign-up")
    public String registerForm(Model model) {
        model.addAttribute("user", new TaiKhoan()); 
        return "fragments/register"; 
    }

    @PostMapping("/sign-up")
    public String register(Model model,
                           @RequestParam("username") String username,
                           @RequestParam("fullname") String fullname,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("message", "Xác nhận mật khẩu không trùng khớp!");
            return "fragments/register";
        }

        if (accountService.findByUsername(username) != null) {
            model.addAttribute("message", "Tên đăng nhập đã tồn tại!");
            return "fragments/register";
        }

        if (accountService.findByEmail(email) != null) {
            model.addAttribute("message", "Email này đã được sử dụng!");
            return "fragments/register";
        }

        try {
            TaiKhoan tk = new TaiKhoan();
            tk.setTenDangNhap(username);
            tk.setMatKhau(accountService.encodePassword(password));
            tk.setHoTen(fullname);
            tk.setEmail(email);

            VaiTro userRole = vaiTroService.getRequiredUserRole();
            tk.replaceRoles(List.of(userRole));
            tk.setAvatar("default.png"); 

            tk.setNgayTao(LocalDateTime.now());
            tk.setTrangThai(true);

            accountService.save(tk);

            return "redirect:/auth/login?success=true"; 

        } catch (Exception e) {
            model.addAttribute("message", "Lỗi hệ thống: " + e.getMessage());
            return "fragments/register";
        }
    }
    
    @GetMapping("/forgot-password")
    public String forgotForm() {
        return "fragments/forgot-password"; 
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(Model model,
                                 @RequestParam("username") String username,
                                 @RequestParam("email") String email) {
        TaiKhoan user = accountService.findByUsername(username);
        if (user == null || user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email.trim())) {
            model.addAttribute("message", "Không tìm thấy tài khoản khớp với thông tin đã nhập!");
            model.addAttribute("messageType", "error");
            return "fragments/forgot-password";
        }

        String oldPassword = user.getMatKhau();
        String temporaryPassword = generateTemporaryPassword();
        user.setMatKhau(accountService.encodePassword(temporaryPassword));
        accountService.save(user);

        String body = "Xin chào " + user.getHoTen() + ",\n\n"
                + "Bạn vừa yêu cầu khôi phục tài khoản tại Gia Thịnh Toys.\n"
                + "Tên đăng nhập: " + user.getTenDangNhap() + "\n"
                + "Mật khẩu tạm thời: " + temporaryPassword + "\n\n"
                + "Vui lòng đăng nhập và đổi mật khẩu ngay để bảo mật tài khoản.\n\n"
                + "Trân trọng.";

        boolean sent = mailService.send(user.getEmail(), "Khôi phục tài khoản - Gia Thịnh Toys", body);
        if (sent) {
            model.addAttribute("message", "Đã gửi mật khẩu tạm thời về email của bạn.");
            model.addAttribute("messageType", "success");
        } else {
            user.setMatKhau(oldPassword);
            accountService.save(user);
            model.addAttribute("message", "Gửi email thất bại. Vui lòng kiểm tra cấu hình mail và thử lại.");
            model.addAttribute("messageType", "error");
        }
        return "fragments/forgot-password";
    }

    private String generateTemporaryPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            builder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return builder.toString();
    }
}
