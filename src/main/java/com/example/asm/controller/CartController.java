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
        var user = authService.getUser();
        model.addAttribute("cart", cartService.getCart(user));
        model.addAttribute("amount", cartService.getAmount(user));
        return "fragments/cart";
    }

    @GetMapping("/add/{id}")
    public String add(@PathVariable("id") Integer idBienThe) {
        try {
            cartService.add(authService.getUser(), idBienThe, 1);
        } catch (Exception e) {
        }
        return "redirect:/cart/view";
    }

    @PostMapping("/add")
    public String add(@RequestParam("variantId") Integer variantId,
                      @RequestParam(value = "qty", defaultValue = "1") int qty) {
        cartService.add(authService.getUser(), variantId, qty);
        return "redirect:/cart/view";
    }

    @PostMapping("/buy-now")
    public String buyNow(@RequestParam("variantId") Integer variantId,
                         @RequestParam(value = "qty", defaultValue = "1") int qty) {
        cartService.add(authService.getUser(), variantId, qty);
        return "redirect:/order/checkout";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer itemKey) {
        cartService.remove(authService.getUser(), itemKey);
        return "redirect:/cart/view";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Integer itemKey, @RequestParam("qty") int qty) {
        cartService.update(authService.getUser(), itemKey, qty);
        return "redirect:/cart/view";
    }

    @GetMapping("/clear")
    public String clear() {
        cartService.clear(authService.getUser());
        return "redirect:/cart/view";
    }
}
