package com.example.asm.controller;

import com.example.asm.entity.MoHinh;
import com.example.asm.service.CategoryService;
import com.example.asm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired CategoryService categoryService;
    @Autowired ProductService productService;
    @ModelAttribute("cates")
    public List<?> getCategories() {
        return categoryService.findAll();
    }

    @GetMapping({"/", "/home/index"})
    public String index(Model model) {
        List<MoHinh> topItems = productService.findOn900();
        Map<String, Integer> defaultVariantIds = new HashMap<>();
        topItems.forEach(item -> {
            var defaultVariant = productService.getDefaultVariant(item);
            defaultVariantIds.put(item.getMaMoHinh(), defaultVariant != null ? defaultVariant.getMaBienThe() : null);
        });
        model.addAttribute("topItems", topItems);
        model.addAttribute("defaultVariantIds", defaultVariantIds);
        return "fragments/home";
    }
}
