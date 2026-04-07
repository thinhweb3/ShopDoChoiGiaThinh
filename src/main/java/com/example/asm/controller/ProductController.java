package com.example.asm.controller;

import com.example.asm.entity.MoHinh;
import com.example.asm.service.CategoryService;
import com.example.asm.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    CategoryService categoryService;

    @ModelAttribute("cates")
    public List<?> getCategories() {
        return categoryService.findAll();
    }

    @GetMapping("/product/list")
    public String list(
            Model model,
            @RequestParam(value = "cid", required = false) Optional<Integer> cid,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "price", required = false) List<String> prices,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "12") int size) {
        List<String> selectedPrices = prices != null ? prices : new ArrayList<>();
        List<MoHinh> allItems = productService.findByFilters(cid, keyword, selectedPrices, sort);
        int pageSize = Math.max(1, Math.min(size, 48));
        int totalItems = allItems.size();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / pageSize);
        int currentPage = totalPages == 0 ? 0 : Math.max(0, Math.min(page, totalPages - 1));
        int fromIndex = totalItems == 0 ? 0 : currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<MoHinh> items = totalItems == 0 ? List.of() : allItems.subList(fromIndex, toIndex);

        Map<String, Long> displayPrices = new HashMap<>();
        for (MoHinh item : items) {
            displayPrices.put(item.getMaMoHinh(), productService.getMinPrice(item));
        }
        model.addAttribute("items", items);
        model.addAttribute("displayPrices", displayPrices);
        model.addAttribute("selectedPrices", selectedPrices);
        model.addAttribute("selectedCid", cid.orElse(null));
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startItem", totalItems == 0 ? 0 : fromIndex + 1);
        model.addAttribute("endItem", toIndex);
        return "product/list";
    }
    
    @GetMapping("/product/list-by-category/{cid}")
    public String listByCategory(@PathVariable("cid") Integer cid) {
        return "redirect:/product/list?cid=" + cid;
    }

    @GetMapping("/product/search")
    public String search(@RequestParam("keyword") String keyword) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        return "redirect:/product/list?keyword=" + encodedKeyword;
    }

    @GetMapping("/product/detail/{id}")
    public String detail(@PathVariable("id") String id, Model model) {

        MoHinh item = productService.findById(id);
        model.addAttribute("item", item);

        if (item != null && item.getDanhMuc() != null) {
            List<MoHinh> related = productService.findByCategoryId(item.getDanhMuc().getMaDanhMuc()).stream()
                    .filter(relatedItem -> !item.getMaMoHinh().equals(relatedItem.getMaMoHinh()))
                    .limit(8)
                    .toList();
            model.addAttribute("relatedProducts", related);
        }

        return "product/detail";
    }
}
