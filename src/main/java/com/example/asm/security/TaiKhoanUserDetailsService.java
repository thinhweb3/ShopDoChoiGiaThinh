package com.example.asm.security;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.TaiKhoanRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaiKhoanUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final TaiKhoanRepository taiKhoanRepository;

    public TaiKhoanUserDetailsService(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
        return new TaiKhoanUserDetails(taiKhoan);
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + user.getUsername()));
        taiKhoan.setMatKhau(newPassword);
        return new TaiKhoanUserDetails(taiKhoanRepository.save(taiKhoan));
    }
}
