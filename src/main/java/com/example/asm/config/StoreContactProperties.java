package com.example.asm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ConfigurationProperties(prefix = "store.contact")
public class StoreContactProperties {

    private String name = "Gia Thinh Shop";
    private String phone = "0900000000";
    private String zalo = "0900000000";
    private String address = "927 Nguy\u1ec5n \u1ea2nh Th\u1ee7, Trung M\u1ef9 T\u00e2y, TPHCM";

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

    public String getAddress() {
        return repairMojibake(address);
    }

    public void setAddress(String address) {
        this.address = address;
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

    private String repairMojibake(String value) {
        if (value == null) {
            return "";
        }
        if (!value.contains("\u00c3") && !value.contains("\u00e1\u00ba") && !value.contains("\u00e1\u00bb")) {
            return value;
        }
        return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }
}
