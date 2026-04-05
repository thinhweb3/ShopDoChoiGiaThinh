package com.example.asm.security;

import com.example.asm.entity.TaiKhoan;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class TaiKhoanUserDetails implements UserDetails, TaiKhoanPrincipal {

    private final TaiKhoan taiKhoan;

    public TaiKhoanUserDetails(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return SecurityAuthorityUtils.buildAuthorities(taiKhoan);
    }

    @Override
    public String getPassword() {
        return taiKhoan.getMatKhau();
    }

    @Override
    public String getUsername() {
        return taiKhoan.getTenDangNhap();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(taiKhoan.getTrangThai());
    }
}
