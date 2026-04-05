package com.example.asm.config;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import com.example.asm.repository.TaiKhoanRepository;
import com.example.asm.security.TaiKhoanPrincipal;
import com.example.asm.service.AuthService;
import com.example.asm.service.AccountService;
import com.example.asm.service.VaiTroService;
import com.example.asm.security.SecurityAuthorityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Configuration
public class GoogleOauth2SupportConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.security.oauth2.client.registration.google",
            name = {"client-id", "client-secret"}
    )
    GoogleOauth2AccountService googleOauth2AccountService(TaiKhoanRepository taiKhoanRepository,
                                                          AccountService accountService,
                                                          VaiTroService vaiTroService) {
        return new GoogleOauth2AccountService(taiKhoanRepository, accountService, vaiTroService);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.security.oauth2.client.registration.google",
            name = {"client-id", "client-secret"}
    )
    GoogleOauth2UserService googleOauth2UserService(GoogleOauth2AccountService googleOauth2AccountService) {
        return new GoogleOauth2UserService(googleOauth2AccountService);
    }

    @Bean
    @Order(0)
    @ConditionalOnProperty(
            prefix = "spring.security.oauth2.client.registration.google",
            name = {"client-id", "client-secret"}
    )
    SecurityFilterChain googleOauth2SecurityFilterChain(HttpSecurity http,
                                                        GoogleOauth2UserService googleOauth2UserService,
                                                        GoogleOauth2AccountService googleOauth2AccountService,
                                                        AuthService authService,
                                                        HttpSessionSecurityContextRepository securityContextRepository) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = savedRequestAwareAuthenticationSuccessHandler();
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .csrf(AbstractHttpConfigurer::disable)
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(googleOauth2UserService))
                        .successHandler((request, response, authentication) -> {
                            try {
                                TaiKhoan localUser = resolveAuthenticatedUser(authentication.getPrincipal(), googleOauth2AccountService);
                                if (localUser == null) {
                                    request.getSession(true).removeAttribute("user");
                                } else {
                                    authService.refreshAuthentication(localUser);
                                }
                                successHandler.onAuthenticationSuccess(
                                        request,
                                        response,
                                        SecurityContextHolder.getContext().getAuthentication()
                                );
                            } catch (IllegalArgumentException | IllegalStateException ex) {
                                SecurityContextHolder.clearContext();
                                request.getSession(true).removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                                request.getSession(true).removeAttribute("user");
                                request.getSession(true).setAttribute("oauth2-error-message", ex.getMessage());
                                response.sendRedirect("/auth/login?oauthError=true");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            request.getSession(true).setAttribute("oauth2-error-message", exception.getMessage());
                            response.sendRedirect("/auth/login?oauthError=true");
                        })
                );
        return http.build();
    }

    private SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/home/index");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }

    private TaiKhoan resolveAuthenticatedUser(Object principal, GoogleOauth2AccountService googleOauth2AccountService) {
        if (principal instanceof TaiKhoanPrincipal taiKhoanPrincipal) {
            return taiKhoanPrincipal.getTaiKhoan();
        }
        if (principal instanceof OAuth2User oauth2User) {
            if (!isEmailVerified(oauth2User)) {
                throw new IllegalArgumentException("Email Google chưa được xác thực.");
            }
            return googleOauth2AccountService.resolveUser(
                    oauth2User.getAttribute("email"),
                    oauth2User.getAttribute("name")
            );
        }
        return null;
    }

    private boolean isEmailVerified(OAuth2User oauth2User) {
        Object emailVerified = oauth2User.getAttribute("email_verified");
        if (emailVerified == null) {
            emailVerified = oauth2User.getAttribute("verified_email");
        }
        if (emailVerified instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (emailVerified instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return true;
    }

    public static class GoogleOauth2AccountService {

        private final TaiKhoanRepository taiKhoanRepository;
        private final AccountService accountService;
        private final VaiTroService vaiTroService;

        public GoogleOauth2AccountService(TaiKhoanRepository taiKhoanRepository,
                                          AccountService accountService,
                                          VaiTroService vaiTroService) {
            this.taiKhoanRepository = taiKhoanRepository;
            this.accountService = accountService;
            this.vaiTroService = vaiTroService;
        }

        public TaiKhoan resolveUser(String email, String fullName) {
            String normalizedEmail = normalizeEmail(email);
            if (!StringUtils.hasText(normalizedEmail)) {
                throw new IllegalArgumentException("Google account không trả về email hợp lệ.");
            }

            TaiKhoan existingUser = taiKhoanRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
            if (existingUser != null) {
                if (!Boolean.TRUE.equals(existingUser.getTrangThai())) {
                    throw new IllegalStateException("Tài khoản này đang bị khóa.");
                }
                return existingUser;
            }

            TaiKhoan newUser = new TaiKhoan();
            newUser.setTenDangNhap(generateUniqueUsername(normalizedEmail));
            newUser.setMatKhau(accountService.encodePassword(UUID.randomUUID().toString()));
            newUser.setHoTen(StringUtils.hasText(fullName) ? fullName.trim() : buildDisplayName(normalizedEmail));
            newUser.setEmail(normalizedEmail);
            newUser.setAvatar("default.png");
            newUser.setTrangThai(true);
            newUser.setNgayTao(LocalDateTime.now());
            VaiTro userRole = vaiTroService.getRequiredUserRole();
            newUser.replaceRoles(List.of(userRole));
            return accountService.save(newUser);
        }

        private String normalizeEmail(String email) {
            return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        }

        private String generateUniqueUsername(String email) {
            String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            String sanitized = localPart.replaceAll("[^a-zA-Z0-9._-]", ".");
            sanitized = sanitized.replaceAll("\\.+", ".");
            sanitized = sanitized.replaceAll("^\\.|\\.$", "");
            if (!StringUtils.hasText(sanitized)) {
                sanitized = "google.user";
            }
            if (sanitized.length() > 40) {
                sanitized = sanitized.substring(0, 40);
            }

            String candidate = sanitized;
            int suffix = 1;
            while (taiKhoanRepository.existsByTenDangNhap(candidate)) {
                String postfix = "." + suffix++;
                int maxBaseLength = Math.max(1, 50 - postfix.length());
                String base = sanitized.length() > maxBaseLength ? sanitized.substring(0, maxBaseLength) : sanitized;
                candidate = base + postfix;
            }
            return candidate;
        }

        private String buildDisplayName(String email) {
            String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            return localPart.replace('.', ' ').replace('_', ' ').trim();
        }
    }

    public static class GoogleOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

        private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        private final GoogleOauth2AccountService googleOauth2AccountService;

        public GoogleOauth2UserService(GoogleOauth2AccountService googleOauth2AccountService) {
            this.googleOauth2AccountService = googleOauth2AccountService;
        }

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            try {
                if (!isEmailVerified(oauth2User)) {
                    throw new IllegalArgumentException("Email Google chưa được xác thực.");
                }
                TaiKhoan taiKhoan = googleOauth2AccountService.resolveUser(email, name);
                return new GoogleTaiKhoanOAuth2User(taiKhoan, oauth2User.getAttributes());
            } catch (IllegalArgumentException | IllegalStateException ex) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("google_account_mapping_failed"),
                        ex.getMessage(),
                        ex
                );
            }
        }

        private boolean isEmailVerified(OAuth2User oauth2User) {
            Object emailVerified = oauth2User.getAttribute("email_verified");
            if (emailVerified == null) {
                emailVerified = oauth2User.getAttribute("verified_email");
            }
            if (emailVerified instanceof Boolean booleanValue) {
                return booleanValue;
            }
            if (emailVerified instanceof String stringValue) {
                return Boolean.parseBoolean(stringValue);
            }
            return true;
        }
    }

    public static class GoogleTaiKhoanOAuth2User implements OAuth2User, TaiKhoanPrincipal {

        private final TaiKhoan taiKhoan;
        private final Map<String, Object> attributes;

        public GoogleTaiKhoanOAuth2User(TaiKhoan taiKhoan, Map<String, Object> attributes) {
            this.taiKhoan = taiKhoan;
            this.attributes = attributes;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return SecurityAuthorityUtils.buildAuthorities(taiKhoan);
        }

        @Override
        public TaiKhoan getTaiKhoan() {
            return taiKhoan;
        }

        @Override
        public String getName() {
            return taiKhoan.getTenDangNhap();
        }
    }
}
