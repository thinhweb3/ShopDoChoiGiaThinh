package com.example.asm.controller;

import com.example.asm.entity.MoHinh;
import com.example.asm.service.CategoryService;
import com.example.asm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

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
        model.addAttribute("topItems", topItems);
        return "fragments/home";
    }
}
