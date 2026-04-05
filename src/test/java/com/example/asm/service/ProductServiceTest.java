package com.example.asm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.asm.entity.MoHinh;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.MoHinhRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductServiceTest {

    private ProductService productService;
    private MoHinhRepository moHinhRepo;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
        moHinhRepo = mock(MoHinhRepository.class);

        ReflectionTestUtils.setField(productService, "moHinhRepo", moHinhRepo);
        ReflectionTestUtils.setField(productService, "bienTheRepo", mock(BienTheMoHinhRepository.class));
    }

    @Test
    void findByFiltersShouldSupportUnder100And100To300Ranges() {
        MoHinh under100 = MoHinh.builder()
                .maMoHinh("MH01")
                .tenMoHinh("Mini A")
                .giaBan(99_000L)
                .trangThai(true)
                .duocBan(true)
                .build();
        MoHinh at100 = MoHinh.builder()
                .maMoHinh("MH02")
                .tenMoHinh("Mini B")
                .giaBan(100_000L)
                .trangThai(true)
                .duocBan(true)
                .build();
        MoHinh at299 = MoHinh.builder()
                .maMoHinh("MH03")
                .tenMoHinh("Mini C")
                .giaBan(299_000L)
                .trangThai(true)
                .duocBan(true)
                .build();
        MoHinh at300 = MoHinh.builder()
                .maMoHinh("MH04")
                .tenMoHinh("Mini D")
                .giaBan(300_000L)
                .trangThai(true)
                .duocBan(true)
                .build();

        when(moHinhRepo.findAll()).thenReturn(List.of(under100, at100, at299, at300));

        List<MoHinh> under100Items = productService.findByFilters(Optional.empty(), null, List.of("under100"), "newest");
        List<MoHinh> between100And300Items = productService.findByFilters(Optional.empty(), null, List.of("100to300"), "newest");

        assertThat(under100Items).extracting(MoHinh::getMaMoHinh).containsExactly("MH01");
        assertThat(between100And300Items).extracting(MoHinh::getMaMoHinh).containsExactlyInAnyOrder("MH02", "MH03");
    }
}
