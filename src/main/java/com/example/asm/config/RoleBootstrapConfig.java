package com.example.asm.config;

import com.example.asm.service.VaiTroService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RoleBootstrapConfig {

    @Bean
    @Order(0)
    CommandLineRunner rolePermissionSchemaRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            Integer columnCount = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.columns
                    WHERE lower(table_name) = lower('VaiTro')
                      AND lower(column_name) = lower('Quyen')
                    """, Integer.class);

            if (columnCount == null || columnCount == 0) {
                jdbcTemplate.execute("""
                        ALTER TABLE VaiTro
                        ADD COLUMN Quyen VARCHAR(2000)
                        """);
            }

            jdbcTemplate.execute("""
                    UPDATE VaiTro
                    SET Quyen = ''
                    WHERE Quyen IS NULL
                    """);
        };
    }

    @Bean
    @Order(1)
    CommandLineRunner roleBootstrapRunner(VaiTroService vaiTroService) {
        return args -> vaiTroService.ensureSystemRoles();
    }
}
