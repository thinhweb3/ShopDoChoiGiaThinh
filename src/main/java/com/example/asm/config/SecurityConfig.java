package com.example.asm.config;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.security.LegacyAwarePasswordEncoder;
import com.example.asm.security.RolePermission;
import com.example.asm.security.SecurityAuthorityUtils;
import com.example.asm.security.TaiKhoanUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new LegacyAwarePasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(TaiKhoanUserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsPasswordService(userDetailsService);
        return new ProviderManager(provider);
    }

    @Bean
    HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            HttpSessionSecurityContextRepository securityContextRepository) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/roles", "/admin/roles/api/**", "/admin/roles/save", "/admin/roles/delete/**")
                        .hasAnyAuthority(
                                "ROLE_" + TaiKhoan.ROLE_SUPER_ADMIN,
                                SecurityAuthorityUtils.permissionAuthority(RolePermission.ROLE_CREATE.getCode()),
                                SecurityAuthorityUtils.permissionAuthority(RolePermission.ROLE_UPDATE.getCode()),
                                SecurityAuthorityUtils.permissionAuthority(RolePermission.ROLE_DELETE.getCode())
                        )
                        .requestMatchers("/", "/home/**", "/product/**", "/admin/login", "/admin/logout", "/auth/login", "/auth/logoff", "/error", "/error/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/admin/**").hasAuthority(SecurityAuthorityUtils.ADMIN_PANEL_AUTHORITY)
                        .requestMatchers("/cart/**", "/favorite/**", "/order/**", "/rating/**", "/account/**").denyAll()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception.accessDeniedPage("/error/403"))
                .formLogin(form -> form.loginPage("/admin/login").permitAll());

        return http.build();
    }
}
