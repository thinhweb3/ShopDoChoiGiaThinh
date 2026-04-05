package com.example.asm.service;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.TaiKhoanRepository;
import com.example.asm.security.TaiKhoanPrincipal;
import com.example.asm.security.TaiKhoanUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    @Autowired HttpSession session;
    @Autowired TaiKhoanRepository taiKhoanRepo;
    @Autowired AuthenticationManager authenticationManager;

    public TaiKhoan login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password)
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            TaiKhoan user = resolveUser(authentication);
            syncSessionUser(user);
            return user;
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            session.removeAttribute("user");
            return null;
        }
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {
        }
    }

    public boolean isLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public TaiKhoan getUser() {
        if (!isLogin()) {
            session.removeAttribute("user");
            return null;
        }

        TaiKhoan user = resolveUser(SecurityContextHolder.getContext().getAuthentication());
        syncSessionUser(user);
        return user;
    }

    public void refreshAuthentication(TaiKhoan user) {
        if (user == null) {
            logout();
            return;
        }

        UserDetails principal = new TaiKhoanUserDetails(user);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        syncSessionUser(user);
    }

    private TaiKhoan resolveUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof TaiKhoanPrincipal taiKhoanPrincipal) {
            TaiKhoan principalUser = taiKhoanPrincipal.getTaiKhoan();
            TaiKhoan resolved = resolveUserFromSnapshot(principalUser);
            if (resolved != null) {
                return resolved;
            }
        }

        TaiKhoan user = resolveUserByUsernameOrEmail(authentication.getName());
        if (user != null) {
            return user;
        }

        if (principal instanceof TaiKhoanUserDetails userDetails) {
            return resolveUserFromSnapshot(userDetails.getTaiKhoan());
        }
        return null;
    }

    private TaiKhoan resolveUserFromSnapshot(TaiKhoan principalUser) {
        if (principalUser == null) {
            return null;
        }

        if (principalUser.getMaTaiKhoan() != null) {
            TaiKhoan byId = taiKhoanRepo.findById(principalUser.getMaTaiKhoan()).orElse(null);
            if (byId != null) {
                return byId;
            }
        }

        TaiKhoan byUsernameOrEmail = resolveUserByUsernameOrEmail(principalUser.getTenDangNhap());
        if (byUsernameOrEmail != null) {
            return byUsernameOrEmail;
        }

        return resolveUserByUsernameOrEmail(principalUser.getEmail());
    }

    private TaiKhoan resolveUserByUsernameOrEmail(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return null;
        }

        TaiKhoan byUsername = taiKhoanRepo.findByTenDangNhap(identifier).orElse(null);
        if (byUsername != null) {
            return byUsername;
        }

        return taiKhoanRepo.findByEmailIgnoreCase(identifier).orElse(null);
    }

    private void syncSessionUser(TaiKhoan user) {
        if (user == null) {
            session.removeAttribute("user");
            return;
        }
        session.setAttribute("user", user);
    }
}
