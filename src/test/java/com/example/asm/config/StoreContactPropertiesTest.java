package com.example.asm.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StoreContactPropertiesTest {

    @Test
    void getAddressShouldRepairMojibakeVietnameseText() {
        StoreContactProperties properties = new StoreContactProperties();
        properties.setAddress("927 Nguy\u00e1\u00bb\u0085n \u00e1\u00ba\u00a2nh Th\u00e1\u00bb\u00a7, Trung M\u00e1\u00bb\u00b9 T\u00c3\u00a2y, TPHCM");

        assertThat(properties.getAddress()).isEqualTo("927 Nguy\u1ec5n \u1ea2nh Th\u1ee7, Trung M\u1ef9 T\u00e2y, TPHCM");
    }
}
