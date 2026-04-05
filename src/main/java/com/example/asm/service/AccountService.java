package com.example.asm.service;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.TaiKhoanRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    @Autowired TaiKhoanRepository taiKhoanRepo;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired VaiTroService vaiTroService;
    
    public List<TaiKhoan> findAll() {
        return taiKhoanRepo.findAll();
    }

    public TaiKhoan findById(Integer id) {
        return taiKhoanRepo.findById(id).orElse(null);
    }

    public TaiKhoan findByUsername(String username) {
        return taiKhoanRepo.findByTenDangNhap(username).orElse(null);
    }
    
    public TaiKhoan findByEmail(String email) {
        return taiKhoanRepo.findByEmail(email).orElse(null);
    }

    public TaiKhoan save(TaiKhoan tk) {
        if (tk != null && (tk.getRoles() == null || tk.getRoles().isEmpty())) {
            tk.replaceRoles(List.of(vaiTroService.getRequiredUserRole()));
        }
        return taiKhoanRepo.save(tk);
    }

    public String encodePassword(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
