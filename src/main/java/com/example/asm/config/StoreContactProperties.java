package com.example.asm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "store.contact")
public class StoreContactProperties {

    private String name = "Gia Thinh Shop";
    private String phone = "0900000000";
    private String zalo = "0900000000";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getZalo() {
        return zalo;
    }

    public void setZalo(String zalo) {
        this.zalo = zalo;
    }

    public String getPhoneDigits() {
        return normalizeDigits(phone);
    }

    public String getZaloDigits() {
        return normalizeDigits(zalo);
    }

    public String getZaloLink() {
        String digits = getZaloDigits();
        return digits.isBlank() ? "#" : "https://zalo.me/" + digits;
    }

    private String normalizeDigits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }
}
