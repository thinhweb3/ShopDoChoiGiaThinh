package com.example.asm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class AsmApplicationTests {

    @Test
    void shouldBeSpringBootApplication() {
        assertThat(AsmApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }
}
