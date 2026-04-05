package com.example.asm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

class ServletInitializerTest {

    @Test
    void configureShouldRegisterAsmApplicationAsSource() {
        ServletInitializer initializer = new ServletInitializer();
        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        SpringApplicationBuilder configured = initializer.configure(builder);

        assertThat(configured.build().getAllSources()).contains(AsmApplication.class);
    }
}
