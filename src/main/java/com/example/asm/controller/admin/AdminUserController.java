package com.example.asm.controller.admin;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import com.example.asm.repository.TaiKhoanRepository;
import com.example.asm.service.AccountService;
import com.example.asm.service.AuthService;
import com.example.asm.service.VaiTroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/accounts")
@PreAuthorize("@adminPermissionGuard.canAccessAccountSection()")
public class AdminUserController {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private VaiTroService vaiTroService;

    private boolean isSuperAdmin(TaiKhoan user) {
        return user != null && user.hasRole(TaiKhoan.ROLE_SUPER_ADMIN);
    }

    @GetMapping
    public String index(Model model) {
        TaiKhoan currentUser = authService != null ? authService.getUser() : null;
        List<TaiKhoan> list = taiKhoanRepository.findAll();
        model.addAttribute("users", list);
        model.addAttribute("availableRoles", vaiTroService.findAllOrdered());
        model.addAttribute("isSuperAdmin", isSuperAdmin(currentUser));
        model.addAttribute("currentUserId", currentUser != null ? currentUser.getMaTaiKhoan() : null);
        model.addAttribute("defaultUserRoleId", vaiTroService.getRequiredUserRole().getMaVaiTro());
        return "admin/admin-user"; 
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserApi(@PathVariable Integer id) {
        return taiKhoanRepository.findById(id)
                .map(user -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("maTaiKhoan", user.getMaTaiKhoan());
                    payload.put("tenDangNhap", user.getTenDangNhap());
                    payload.put("hoTen", user.getHoTen());
                    payload.put("email", user.getEmail());
                    payload.put("soDienThoai", user.getSoDienThoai());
                    payload.put("roleIds", user.getSortedRoleIds());
                    payload.put("roleCodes", user.getSortedRoleCodes());
                    payload.put("roleDisplayText", user.getRoleDisplayText());
                    return ResponseEntity.ok(payload);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.canSaveAccount(#tk)")
    public String save(@ModelAttribute TaiKhoan tk,
                       @RequestParam(value = "roleIds", required = false) List<Integer> roleIds,
                       RedirectAttributes params) {
        try {
            TaiKhoan currentUser = authService != null ? authService.getUser() : null;
            boolean currentIsSuperAdmin = isSuperAdmin(currentUser);
            boolean blockedSelfRoleChange = false;

            if (tk.getMaTaiKhoan() == null) {
                if (taiKhoanRepository.existsByTenDangNhap(tk.getTenDangNhap())) {
                    params.addFlashAttribute("message", "Tên đăng nhập đã tồn tại!");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/accounts";
                }
                if (tk.getMatKhau() == null || tk.getMatKhau().isBlank()) {
                    params.addFlashAttribute("message", "Mật khẩu không được để trống!");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/accounts";
                }
                
                tk.setNgayTao(LocalDateTime.now());
                tk.setTrangThai(true);
                tk.setAvatar("default.png");
                tk.setMatKhau(accountService.encodePassword(tk.getMatKhau()));
                List<VaiTro> requestedRoles = currentIsSuperAdmin
                        ? vaiTroService.resolveRolesByIds(roleIds, true)
                        : List.of(vaiTroService.getRequiredUserRole());
                tk.replaceRoles(requestedRoles);
                
                params.addFlashAttribute("message", "Thêm mới thành công!");
            } else {
                TaiKhoan oldUser = taiKhoanRepository.findById(tk.getMaTaiKhoan()).orElse(null);
                if (oldUser != null) {
                    tk.setTenDangNhap(oldUser.getTenDangNhap());
                    tk.setMatKhau(oldUser.getMatKhau());
                    tk.setNgayTao(oldUser.getNgayTao());
                    tk.setAvatar(oldUser.getAvatar());
                    tk.setTrangThai(oldUser.getTrangThai());

                    boolean editingSelf = currentUser != null && currentUser.getMaTaiKhoan().equals(oldUser.getMaTaiKhoan());
                    List<VaiTro> requestedRoles = currentIsSuperAdmin
                            ? vaiTroService.resolveRolesByIds(roleIds, true)
                            : List.of(vaiTroService.getRequiredUserRole());
                    tk.replaceRoles(requestedRoles);
                    if (!currentIsSuperAdmin || editingSelf) {
                        if (currentIsSuperAdmin && editingSelf && !tk.getSortedRoleIds().equals(oldUser.getSortedRoleIds())) {
                            blockedSelfRoleChange = true;
                        }
                        tk.replaceRoles(oldUser.getSortedRoles());
                    } else if (tk.getSortedRoles().isEmpty()) {
                        tk.replaceRoles(oldUser.getSortedRoles());
                    }
                } else {
                    params.addFlashAttribute("message", "Không tìm thấy tài khoản cần cập nhật!");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/accounts";
                }
                if (blockedSelfRoleChange) {
                    params.addFlashAttribute("message", "Không thể tự đổi vai trò của chính bạn. Thông tin khác đã được cập nhật.");
                } else {
                    params.addFlashAttribute("message", "Cập nhật thành công!");
                }
            }
            
            params.addFlashAttribute("messageType", "success");
            TaiKhoan savedUser = taiKhoanRepository.save(tk);
            if (currentUser != null && currentUser.getMaTaiKhoan().equals(savedUser.getMaTaiKhoan())) {
                if (Boolean.TRUE.equals(savedUser.getTrangThai())) {
                    authService.refreshAuthentication(savedUser);
                } else {
                    authService.logout();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/accounts";
    }

    public String save(TaiKhoan tk, RedirectAttributes params) {
        return save(tk, null, params);
    }

    @GetMapping("/toggle-status/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('ACCOUNT_LOCK')")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes params) {
        TaiKhoan tk = taiKhoanRepository.findById(id).orElse(null);
        if (tk != null) {

            tk.setTrangThai(!tk.getTrangThai());
            taiKhoanRepository.save(tk);
            
            String status = tk.getTrangThai() ? "Mở khóa" : "Khóa";
            params.addFlashAttribute("message", "Đã " + status + " tài khoản: " + tk.getTenDangNhap());
            params.addFlashAttribute("messageType", "success");
        } else {
            params.addFlashAttribute("message", "Không tìm thấy tài khoản!");
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/accounts";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('ACCOUNT_DELETE')")
    public String delete(@PathVariable Integer id, RedirectAttributes params) {
        try {
            taiKhoanRepository.deleteById(id);
            params.addFlashAttribute("message", "Xóa tài khoản thành công!");
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Không thể xóa tài khoản này vì đã có dữ liệu liên quan! Hãy dùng chức năng Khóa.");
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/accounts";
    }
}
