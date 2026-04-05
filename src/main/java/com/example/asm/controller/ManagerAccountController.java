package com.example.asm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.asm.repository.TaiKhoanRepository;
import com.example.asm.service.AccountService;


@Controller
@RequestMapping("/account")
public class ManagerAccountController {

    @Autowired TaiKhoanRepository taiKhoanRepo;
    @Autowired AccountService accountService;
    
    @ModelAttribute("accounts")
    public Object getAccounts() { return accountService.findAll(); }
    
    @GetMapping("/view")
    public String view(Model model) {
    	model.addAttribute("accounts", taiKhoanRepo.findAll());
    	return "fragments/managerAccount";
    }
    

}