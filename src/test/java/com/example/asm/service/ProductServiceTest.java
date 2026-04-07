package com.example.asm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.MoHinh;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.MoHinhRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductServiceTest {

    private ProductService productService;
    private MoHinhRepository moHinhRepo;
    private BienTheMoHinhRepository bienTheRepo;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
        moHinhRepo = mock(MoHinhRepository.class);
        bienTheRepo = mock(BienTheMoHinhRepository.class);

        ReflectionTestUtils.setField(productService, "moHinhRepo", moHinhRepo);
        ReflectionTestUtils.setField(productService, "bienTheRepo", bienTheRepo);
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

    @Test
    void getDefaultVariantShouldCreateHiddenVariantFromProductStock() {
        MoHinh product = MoHinh.builder()
                .maMoHinh("P1")
                .giaBan(150_000L)
                .tonKho(4)
                .trangThai(true)
                .duocBan(true)
                .build();
        when(bienTheRepo.findByMoHinh_MaMoHinh("P1")).thenReturn(List.of());
        when(bienTheRepo.save(any(BienTheMoHinh.class))).thenAnswer(inv -> {
            BienTheMoHinh variant = inv.getArgument(0);
            variant.setMaBienThe(9);
            return variant;
        });

        BienTheMoHinh variant = productService.getDefaultVariant(product);

        assertThat(variant.getMaBienThe()).isEqualTo(9);
        assertThat(variant.getMoHinh()).isEqualTo(product);
        assertThat(variant.getKichThuoc()).isEqualTo("Mặc định");
        assertThat(variant.getGiaBan()).isEqualTo(150_000L);
        assertThat(variant.getSoLuongTon()).isEqualTo(4);
        assertThat(variant.getSku()).isEqualTo("P1");
        assertThat(variant.getTinhTrang()).isEqualTo("Còn hàng");
    }

    @Test
    void getDefaultVariantShouldNotCreateVariantWhenProductOutOfStock() {
        MoHinh product = MoHinh.builder()
                .maMoHinh("P1")
                .giaBan(150_000L)
                .tonKho(0)
                .trangThai(true)
                .duocBan(true)
                .build();

        BienTheMoHinh variant = productService.getDefaultVariant(product);

        assertThat(variant).isNull();
        verify(bienTheRepo, never()).save(any(BienTheMoHinh.class));
    }
}
