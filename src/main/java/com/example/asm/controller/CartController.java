package com.example.asm.controller;

import com.example.asm.service.AuthService;
import com.example.asm.service.CartService;
import com.example.asm.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
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
    public String add(@PathVariable("id") Integer idBienThe,
                      @RequestHeader(value = "referer", required = false) String referer,
                      RedirectAttributes params) {
        try {
            cartService.add(authService.getUser(), idBienThe, 1);
            params.addFlashAttribute("cartMessage", "Đã thêm vào giỏ hàng.");
            params.addFlashAttribute("cartMessageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("cartMessage", e.getMessage() == null ? "Không thể thêm vào giỏ hàng." : e.getMessage());
            params.addFlashAttribute("cartMessageType", "danger");
        }
        return redirectBack(referer);
    }

    @PostMapping("/add")
    public String add(@RequestParam("variantId") Integer variantId,
                      @RequestParam(value = "qty", defaultValue = "1") int qty,
                      @RequestHeader(value = "referer", required = false) String referer,
                      RedirectAttributes params) {
        try {
            cartService.add(authService.getUser(), variantId, qty);
            params.addFlashAttribute("cartMessage", "Đã thêm vào giỏ hàng.");
            params.addFlashAttribute("cartMessageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("cartMessage", e.getMessage() == null ? "Không thể thêm vào giỏ hàng." : e.getMessage());
            params.addFlashAttribute("cartMessageType", "danger");
        }
        return redirectBack(referer);
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

    private String redirectBack(String referer) {
        return "redirect:" + safeReturnPath(referer);
    }

    private String safeReturnPath(String referer) {
        if (referer == null || referer.isBlank()) {
            return "/product/list";
        }

        try {
            URI uri = URI.create(referer.trim());
            String path = uri.getRawPath();
            if (path == null || path.isBlank() || !path.startsWith("/") || path.startsWith("//")) {
                return "/product/list";
            }
            if (path.startsWith("/cart/add") || path.startsWith("/cart/buy-now")) {
                return "/product/list";
            }

            String query = uri.getRawQuery();
            return query == null || query.isBlank() ? path : path + "?" + query;
        } catch (IllegalArgumentException e) {
            return "/product/list";
        }
    }
}
