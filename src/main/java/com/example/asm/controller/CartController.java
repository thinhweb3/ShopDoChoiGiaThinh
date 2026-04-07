package com.example.asm.controller;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.MoHinh;
import com.example.asm.service.AuthService;
import com.example.asm.service.CartService;
import com.example.asm.service.CategoryService;
import com.example.asm.service.ProductService;
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
    @Autowired ProductService productService;

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

    @GetMapping("/add-product/{id}")
    public String addProduct(@PathVariable("id") String productId,
                             @RequestHeader(value = "referer", required = false) String referer,
                             RedirectAttributes params) {
        try {
            addProductToCart(productId, 1);
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

    @PostMapping("/add-product")
    public String addProduct(@RequestParam("productId") String productId,
                             @RequestParam(value = "qty", defaultValue = "1") int qty,
                             @RequestHeader(value = "referer", required = false) String referer,
                             RedirectAttributes params) {
        try {
            addProductToCart(productId, qty);
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

    @PostMapping("/buy-now-product")
    public String buyNowProduct(@RequestParam("productId") String productId,
                                @RequestParam(value = "qty", defaultValue = "1") int qty,
                                RedirectAttributes params) {
        try {
            addProductToCart(productId, qty);
            return "redirect:/order/checkout";
        } catch (Exception e) {
            params.addFlashAttribute("cartMessage", e.getMessage() == null ? "Không thể mua ngay." : e.getMessage());
            params.addFlashAttribute("cartMessageType", "danger");
            return "redirect:/product/detail/" + productId;
        }
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

    private void addProductToCart(String productId, int qty) {
        BienTheMoHinh defaultVariant = getOrderableDefaultVariant(productId);
        cartService.add(authService.getUser(), defaultVariant.getMaBienThe(), qty);
    }

    private BienTheMoHinh getOrderableDefaultVariant(String productId) {
        MoHinh product = productService.findById(productId);
        if (product == null) {
            throw new RuntimeException("Đồ chơi không tồn tại");
        }
        BienTheMoHinh defaultVariant = productService.getDefaultVariant(product);
        if (defaultVariant == null || defaultVariant.getMaBienThe() == null) {
            throw new RuntimeException("Sản phẩm này hiện đã hết hàng.");
        }
        return defaultVariant;
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
            if (path.startsWith("/cart/add") || path.startsWith("/cart/add-product")
                    || path.startsWith("/cart/buy-now") || path.startsWith("/cart/buy-now-product")) {
                return "/product/list";
            }

            String query = uri.getRawQuery();
            return query == null || query.isBlank() ? path : path + "?" + query;
        } catch (IllegalArgumentException e) {
            return "/product/list";
        }
    }
}
