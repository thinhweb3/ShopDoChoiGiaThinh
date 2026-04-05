package com.example.asm.service;

import com.example.asm.entity.DanhMuc;
import com.example.asm.repository.DanhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    @Autowired DanhMucRepository danhMucRepo;

    public List<DanhMuc> findAll() {
        return danhMucRepo.findAll();
    }

    public DanhMuc findById(Integer id) {
        return danhMucRepo.findById(id).orElse(null);
    }
}