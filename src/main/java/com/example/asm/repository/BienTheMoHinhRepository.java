package com.example.asm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.asm.entity.BienTheMoHinh;

public interface BienTheMoHinhRepository extends JpaRepository<BienTheMoHinh, Integer> {
    List<BienTheMoHinh> findByMoHinh_MaMoHinh(String maMoHinh);
    Optional<BienTheMoHinh> findBySku(String sku);
    
}
