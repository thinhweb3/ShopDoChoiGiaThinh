package com.example.asm.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssetUrlServiceTest {

    private final AssetUrlService assetUrlService = new AssetUrlService();

    @Test
    void productShouldUseLogoWhenValueMissing() {
        assertThat(assetUrlService.product(null)).isEqualTo("/images/logo.jpg");
    }

    @Test
    void productShouldUseLogoForStoredImageValue() {
        String url = "https://cdn.example.com/demo.png";
        assertThat(assetUrlService.product(url)).isEqualTo("/images/logo.jpg");
    }

    @Test
    void avatarShouldIgnoreLegacyDefaultPlaceholder() {
        assertThat(assetUrlService.avatar("default.png"))
                .isEqualTo("https://ui-avatars.com/api/?name=User&background=random&color=fff");
    }
}
