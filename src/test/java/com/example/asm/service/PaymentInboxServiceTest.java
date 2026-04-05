package com.example.asm.service;

import com.example.asm.entity.DonHang;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentInboxServiceTest {

    private PaymentInboxService paymentInboxService;

    @BeforeEach
    void setUp() {
        paymentInboxService = new PaymentInboxService(
                "imap.gmail.com",
                993,
                "shop@gmail.com",
                "app-password",
                "INBOX",
                1,
                48,
                30,
                "");
    }

    @Test
    void matchesPaymentEmailTextShouldAcceptOrderCodeAndAmountWithSeparators() {
        DonHang order = DonHang.builder()
                .maDonHang(88)
                .tongTien(230_000L)
                .build();

        boolean matched = paymentInboxService.matchesPaymentEmailText(
                "Thong bao bao co",
                "Ngan hang vua nhan tien voi noi dung DH 88, so tien 230.000 VND.",
                order);

        assertThat(matched).isTrue();
    }

    @Test
    void matchesPaymentEmailTextShouldRejectWhenAmountDoesNotMatch() {
        DonHang order = DonHang.builder()
                .maDonHang(90)
                .tongTien(430_000L)
                .build();

        boolean matched = paymentInboxService.matchesPaymentEmailText(
                "Bao co DH90",
                "So tien nhan duoc la 430.001 VND.",
                order);

        assertThat(matched).isFalse();
    }

    @Test
    void matchesPaymentEmailTextShouldRejectWhenTransferCodeMissing() {
        DonHang order = DonHang.builder()
                .maDonHang(91)
                .tongTien(500_000L)
                .build();

        boolean matched = paymentInboxService.matchesPaymentEmailText(
                "Bao co tai khoan",
                "So tien 500,000 VND da vao tai khoan.",
                order);

        assertThat(matched).isFalse();
    }
}
