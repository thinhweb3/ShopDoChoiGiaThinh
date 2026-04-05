package com.example.asm.repository;

import com.example.asm.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VaiTroRepository extends JpaRepository<VaiTro, Integer> {

    Optional<VaiTro> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
