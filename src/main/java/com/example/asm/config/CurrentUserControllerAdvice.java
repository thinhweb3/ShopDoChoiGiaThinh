package com.example.asm.config;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserControllerAdvice {

    private final AuthService authService;
    private final StoreContactProperties storeContactProperties;

    public CurrentUserControllerAdvice(AuthService authService, StoreContactProperties storeContactProperties) {
        this.authService = authService;
        this.storeContactProperties = storeContactProperties;
    }

    @ModelAttribute("currentUser")
    public TaiKhoan currentUser(HttpSession session) {
        TaiKhoan user = authService.getUser();
        if (user == null) {
            session.removeAttribute("user");
        } else {
            session.setAttribute("user", user);
        }
        return user;
    }

    @ModelAttribute("storeContactName")
    public String storeContactName() {
        return storeContactProperties.getName();
    }

    @ModelAttribute("storeContactPhone")
    public String storeContactPhone() {
        return storeContactProperties.getPhone();
    }

    @ModelAttribute("storeContactPhoneDigits")
    public String storeContactPhoneDigits() {
        return storeContactProperties.getPhoneDigits();
    }

    @ModelAttribute("storeContactZalo")
    public String storeContactZalo() {
        return storeContactProperties.getZalo();
    }

    @ModelAttribute("storeContactZaloLink")
    public String storeContactZaloLink() {
        return storeContactProperties.getZaloLink();
    }
}
