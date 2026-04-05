package com.example.asm.controller;

import com.example.asm.service.AuthService;
import com.example.asm.service.CartService;
import com.example.asm.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired CartService cartService;
    @Autowired AuthService authService;
    @Autowired CategoryService categoryService;

    @ModelAttribute("cates")
    public List<?> getCategories() { return categoryService.findAll(); }

    @GetMapping("/view")
    public String view(Model model) {
        if (!authService.isLogin()) return "redirect:/auth/login";
        
        var user = authService.getUser();
        model.addAttribute("cart", cartService.getCart(user));
        model.addAttribute("amount", cartService.getAmount(user));
        return "fragments/cart";
    }

    @GetMapping("/add/{id}")
    public String add(@PathVariable("id") Integer idBienThe) {
        if (!authService.isLogin()) return "redirect:/auth/login";
        try {
            cartService.add(authService.getUser(), idBienThe, 1);
        } catch (Exception e) {
        }
        return "redirect:/cart/view";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer idGioHang) {
        if (authService.isLogin()) cartService.remove(idGioHang);
        return "redirect:/cart/view";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Integer idGioHang, @RequestParam("qty") int qty) {
        if (authService.isLogin()) cartService.update(idGioHang, qty);
        return "redirect:/cart/view";
    }

    @GetMapping("/clear")
    public String clear() {
        if (authService.isLogin()) cartService.clear(authService.getUser());
        return "redirect:/cart/view";
    }
}