package com.example.asm.controller;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.service.AuthService;
import com.example.asm.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RatingController {

    @Autowired
    private RatingService ratingService;
    
    @Autowired
    private AuthService authService;

    @PostMapping("/rating/add")
    public String addRating(@RequestParam("maMoHinh") String maMoHinh,
                            @RequestParam("rating") Byte stars,
                            @RequestParam("comment") String comment,
                            RedirectAttributes params) {
        
        if (!authService.isLogin()) {
            return "redirect:/auth/login";
        }

        try {
            TaiKhoan user = authService.getUser();
            ratingService.addRating(user, maMoHinh, stars, comment);
            params.addFlashAttribute("message", "Cảm ơn bạn đã đánh giá!");
        } catch (Exception e) {
            params.addFlashAttribute("error", "Lỗi khi gửi đánh giá: " + e.getMessage());
        }

        return "redirect:/product/detail/" + maMoHinh;
    }
}