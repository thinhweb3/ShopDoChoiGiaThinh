package com.example.asm.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.example.asm.entity.DanhMuc;
import com.example.asm.entity.MoHinh;
import com.example.asm.service.CategoryService;
import com.example.asm.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class HomeControllerTest {

    @Test
    void indexShouldReturnHomeViewAndModelAttributes() {
        List<DanhMuc> categories = List.of(DanhMuc.builder().maDanhMuc(1).tenDanhMuc("A").build());
        List<MoHinh> topItems = List.of(MoHinh.builder().maMoHinh("MH001").tenMoHinh("Gundam").build());

        HomeController homeController = new HomeController();
        homeController.categoryService = new CategoryService() {
            @Override
            public List<DanhMuc> findAll() {
                return categories;
            }
        };
        homeController.productService = new ProductService() {
            @Override
            public List<MoHinh> findOn900() {
                return topItems;
            }
        };

        Model model = new ExtendedModelMap();
        String view = homeController.index(model);

        assertThat(view).isEqualTo("fragments/home");
        assertThat(homeController.getCategories()).isEqualTo(categories);
        assertThat(model.getAttribute("topItems")).isEqualTo(topItems);
    }

    @Test
    void getCategoriesShouldReturnDataFromCategoryService() {
        List<DanhMuc> categories = List.of(DanhMuc.builder().maDanhMuc(2).tenDanhMuc("B").build());
        HomeController homeController = new HomeController();
        homeController.categoryService = new CategoryService() {
            @Override
            public List<DanhMuc> findAll() {
                return categories;
            }
        };

        assertThat(homeController.getCategories()).isEqualTo(categories);
    }
}
