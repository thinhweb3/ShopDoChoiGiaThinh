package com.example.asm.controller;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired AuthService authService;

    @GetMapping({"/admin/login", "/auth/login"})
    public String form(@RequestParam(value = "success", required = false) Boolean success,
                       @RequestParam(value = "logout", required = false) Boolean logout,
                       @RequestParam(value = "oauthError", required = false) Boolean oauthError,
                       HttpSession session,
                       Model model) {
        TaiKhoan currentUser = authService.getUser();
        if (currentUser != null) {
            return currentUser.hasAdminPanelAccess() ? "redirect:/admin/dashboard" : "redirect:/home/index";
        }

        if (Boolean.TRUE.equals(logout)) {
            model.addAttribute("infoMessage", "Phiên quản trị đã được đăng xuất.");
        }

        if (Boolean.TRUE.equals(success)) {
            model.addAttribute("successMessage", "Tài khoản đã được tạo. Bạn có thể đăng nhập quản trị nếu tài khoản được cấp quyền.");
        }
        if (Boolean.TRUE.equals(oauthError)) {
            model.addAttribute("message", "Đăng nhập Google không còn được bật trên giao diện hiện tại.");
        }

        return "fragments/login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(Model model,
                             @RequestParam("username") String username,
                             @RequestParam("password") String password,
                             HttpSession session) {
        TaiKhoan user = authService.login(username, password);
        if (user == null) {
            model.addAttribute("message", "Sai tài khoản hoặc mật khẩu!");
            return "fragments/login";
        }

        if (!user.hasAdminPanelAccess()) {
            authService.logout();
            model.addAttribute("message", "Tai khoan nay khong co quyen quan tri.");
            return "fragments/login";
        }

        session.removeAttribute("security-uri");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/auth/login")
    public String login(Model model,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session) {
        TaiKhoan user = authService.login(username, password);
        if (user == null) {
            model.addAttribute("message", "Sai tài khoản hoặc mật khẩu!");
            return "fragments/login";
        }

        String backUrl = (String) session.getAttribute("security-uri");
        if (backUrl != null) {
            session.removeAttribute("security-uri");
            return "redirect:" + backUrl;
        }
        return user.hasAdminPanelAccess() ? "redirect:/admin/dashboard" : "redirect:/home/index";
    }

    @GetMapping({"/admin/logout", "/auth/logoff"})
    public String logoff() {
        authService.logout();
        return "redirect:/admin/login?logout=true";
    }
}
