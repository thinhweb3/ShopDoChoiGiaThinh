package com.example.asm.service;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

@Service("assetUrlService")
public class AssetUrlService {

    private static final String DEFAULT_PRODUCT_IMAGE = "/images/banner-bg.jpg";
    private static final String DEFAULT_AVATAR =
            "https://ui-avatars.com/api/?name=User&background=random&color=fff";

    public String product(String value) {
        return resolve(value, DEFAULT_PRODUCT_IMAGE, false);
    }

    public String avatar(String value) {
        return resolve(value, DEFAULT_AVATAR, true);
    }

    private String resolve(String value, String fallback, boolean treatLegacyDefaultAsMissing) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }

        String normalized = value.trim();
        if (treatLegacyDefaultAsMissing
                && ("default.png".equalsIgnoreCase(normalized) || "no-image.png".equalsIgnoreCase(normalized))) {
            return fallback;
        }
        if ("banner-bg.jpg".equalsIgnoreCase(normalized)) {
            return DEFAULT_PRODUCT_IMAGE;
        }
        if (normalized.startsWith("http://")
                || normalized.startsWith("https://")
                || normalized.startsWith("//")
                || normalized.startsWith("data:")) {
            return normalized;
        }
        if (normalized.startsWith("/")) {
            return normalized;
        }
        return "/images/" + UriUtils.encodePath(normalized, StandardCharsets.UTF_8);
    }
}
