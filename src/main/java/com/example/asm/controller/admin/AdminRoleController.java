package com.example.asm.controller.admin;

import com.example.asm.entity.TaiKhoan;
import com.example.asm.entity.VaiTro;
import com.example.asm.repository.TaiKhoanRepository;
import com.example.asm.repository.VaiTroRepository;
import com.example.asm.security.RolePermission;
import com.example.asm.service.AuthService;
import com.example.asm.service.VaiTroService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/roles")
@PreAuthorize("@adminPermissionGuard.canAccessRoleSection()")
public class AdminRoleController {

    private final VaiTroService vaiTroService;
    private final VaiTroRepository vaiTroRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final AuthService authService;

    public AdminRoleController(VaiTroService vaiTroService,
                               VaiTroRepository vaiTroRepository,
                               TaiKhoanRepository taiKhoanRepository,
                               AuthService authService) {
        this.vaiTroService = vaiTroService;
        this.vaiTroRepository = vaiTroRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.authService = authService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("roles", vaiTroService.findAllOrdered());
        model.addAttribute("permissionGroups", vaiTroService.getPermissionGroups());
        return "admin/admin-role";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRoleApi(@PathVariable Integer id) {
        VaiTro role = vaiTroService.findById(id);
        if (role == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("maVaiTro", role.getMaVaiTro());
        payload.put("code", role.getCode());
        payload.put("tenHienThi", role.getTenHienThi());
        payload.put("moTa", role.getMoTa());
        payload.put("permissionCodes", role.getPermissionCodes());
        payload.put("permissionSummaryText", role.getPermissionSummaryText());
        payload.put("choPhepTruyCapAdmin", role.getChoPhepTruyCapAdmin());
        payload.put("laVaiTroHeThong", role.getLaVaiTroHeThong());
        payload.put("soTaiKhoan", taiKhoanRepository.countByRoles_MaVaiTro(role.getMaVaiTro()));
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/save")
    @PreAuthorize("@adminPermissionGuard.canSaveRole(#formRole)")
    public String save(@ModelAttribute VaiTro formRole,
                       @RequestParam(value = "permissionCodes", required = false) List<String> permissionCodes,
                       RedirectAttributes params) {
        String normalizedCode = vaiTroService.normalizeCode(formRole.getCode());
        String displayName = normalizeText(formRole.getTenHienThi());
        String description = normalizeText(formRole.getMoTa());

        if (!StringUtils.hasText(displayName)) {
            params.addFlashAttribute("message", "Tên hiển thị vai trò không được để trống.");
            params.addFlashAttribute("messageType", "error");
            return "redirect:/admin/roles";
        }

        if (!StringUtils.hasText(description)) {
            params.addFlashAttribute("message", "Mô tả vai trò không được để trống.");
            params.addFlashAttribute("messageType", "error");
            return "redirect:/admin/roles";
        }

        if (formRole.getMaVaiTro() == null && !StringUtils.hasText(normalizedCode)) {
            params.addFlashAttribute("message", "Mã quyền chỉ được chứa chữ cái, số hoặc dấu gạch dưới.");
            params.addFlashAttribute("messageType", "error");
            return "redirect:/admin/roles";
        }

        try {
            VaiTro savedRole;
            if (formRole.getMaVaiTro() == null) {
                if (vaiTroService.codeExistsForAnotherRole(normalizedCode, null)) {
                    params.addFlashAttribute("message", "Mã quyền đã tồn tại.");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/roles";
                }

                VaiTro newRole = VaiTro.builder()
                        .code(normalizedCode)
                        .tenHienThi(displayName)
                        .moTa(description)
                        .quyen("")
                        .choPhepTruyCapAdmin(Boolean.TRUE.equals(formRole.getChoPhepTruyCapAdmin()))
                        .laVaiTroHeThong(false)
                        .build();
                newRole.setPermissionCodes(resolveEffectivePermissionCodes(newRole, permissionCodes));
                savedRole = vaiTroService.save(newRole);
                params.addFlashAttribute("message", "Thêm quyền mới thành công!");
            } else {
                VaiTro existingRole = vaiTroService.findById(formRole.getMaVaiTro());
                if (existingRole == null) {
                    params.addFlashAttribute("message", "Không tìm thấy quyền cần cập nhật.");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/roles";
                }

                boolean systemRole = Boolean.TRUE.equals(existingRole.getLaVaiTroHeThong());
                String effectiveCode = systemRole ? existingRole.getCode() : normalizedCode;
                if (!StringUtils.hasText(effectiveCode)) {
                    params.addFlashAttribute("message", "Mã quyền chỉ được chứa chữ cái, số hoặc dấu gạch dưới.");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/roles";
                }
                if (vaiTroService.codeExistsForAnotherRole(effectiveCode, existingRole.getMaVaiTro())) {
                    params.addFlashAttribute("message", "Mã quyền đã tồn tại.");
                    params.addFlashAttribute("messageType", "error");
                    return "redirect:/admin/roles";
                }

                existingRole.setCode(effectiveCode);
                existingRole.setTenHienThi(displayName);
                existingRole.setMoTa(description);
                existingRole.setPermissionCodes(resolveEffectivePermissionCodes(existingRole, permissionCodes));
                existingRole.setLaVaiTroHeThong(systemRole);
                existingRole.setChoPhepTruyCapAdmin(shouldGrantAdminAccess(existingRole, formRole));
                savedRole = vaiTroService.save(existingRole);
                params.addFlashAttribute("message", "Cập nhật quyền thành công!");
            }

            refreshCurrentAuthenticationIfNeeded(savedRole);
            params.addFlashAttribute("messageType", "success");
        } catch (Exception ex) {
            params.addFlashAttribute("message", "Lỗi: " + ex.getMessage());
            params.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin/roles";
    }

    public String save(VaiTro formRole, RedirectAttributes params) {
        return save(formRole, null, params);
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('ROLE_DELETE')")
    public String delete(@PathVariable Integer id, RedirectAttributes params) {
        VaiTro role = vaiTroService.findById(id);
        if (role == null) {
            params.addFlashAttribute("message", "Không tìm thấy quyền.");
            params.addFlashAttribute("messageType", "error");
            return "redirect:/admin/roles";
        }

        if (Boolean.TRUE.equals(role.getLaVaiTroHeThong())) {
            params.addFlashAttribute("message", "Không thể xóa quyền hệ thống mặc định.");
            params.addFlashAttribute("messageType", "error");
            return "redirect:/admin/roles";
        }

        long assignedUsers = taiKhoanRepository.countByRoles_MaVaiTro(role.getMaVaiTro());
        if (assignedUsers > 0) {
            params.addFlashAttribute("message", "Không thể xóa quyền đang được gán cho tài khoản.");
            params.addFlashAttribute("messageType", "error");
            return "redirect:/admin/roles";
        }

        vaiTroRepository.deleteById(id);
        params.addFlashAttribute("message", "Xóa quyền thành công!");
        params.addFlashAttribute("messageType", "success");
        return "redirect:/admin/roles";
    }

    private boolean shouldGrantAdminAccess(VaiTro existingRole, VaiTro formRole) {
        if (TaiKhoan.ROLE_SUPER_ADMIN.equalsIgnoreCase(existingRole.getCode())) {
            return true;
        }
        return Boolean.TRUE.equals(formRole.getChoPhepTruyCapAdmin());
    }

    private Collection<String> resolveEffectivePermissionCodes(VaiTro role, Collection<String> permissionCodes) {
        if (role != null && TaiKhoan.ROLE_SUPER_ADMIN.equalsIgnoreCase(role.getCode())) {
            return RolePermission.allCodes();
        }
        return vaiTroService.resolvePermissionCodes(permissionCodes);
    }

    private void refreshCurrentAuthenticationIfNeeded(VaiTro savedRole) {
        if (savedRole == null || savedRole.getMaVaiTro() == null || authService == null) {
            return;
        }

        TaiKhoan currentUser = authService.getUser();
        if (currentUser == null || !currentUser.getSortedRoleIds().contains(savedRole.getMaVaiTro())) {
            return;
        }

        TaiKhoan refreshedUser = taiKhoanRepository.findById(currentUser.getMaTaiKhoan()).orElse(currentUser);
        authService.refreshAuthentication(refreshedUser);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}
