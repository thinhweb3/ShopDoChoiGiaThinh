package com.example.asm.controller.admin;

import com.example.asm.entity.ChiTietDonHang;
import com.example.asm.entity.DonHang;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.security.RolePermission;
import com.example.asm.repository.ChiTietDonHangRepository;
import com.example.asm.repository.DonHangRepository;
import com.example.asm.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("@adminPermissionGuard.canAccessOrderSection()")
public class AdminOrderController {

    private static final List<String> ORDER_GROUP_NEW = List.of("Đặt hàng", "Chờ xử lý", "Đã xác nhận");
    private static final List<String> ORDER_GROUP_SHIPPING = List.of("Đang giao", "Đang giao hàng");
    private static final List<String> ORDER_GROUP_DELIVERED = List.of("Đã giao", "Hoàn thành");
    private static final List<String> ORDER_GROUP_CANCELLED = List.of("Đã hủy");

    @Autowired
    private DonHangRepository donHangRepo;

    @Autowired
    private ChiTietDonHangRepository chiTietRepo;

    @Autowired
    private OrderService orderService;

    /**
     * Danh sách đơn hàng với phân trang, tìm kiếm, lọc
     */
    @GetMapping
    public String index(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String trangThai,
                       @RequestParam(defaultValue = "ngayDat") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DonHang> orderPage;
        List<String> statusFilter = resolveStatusFilter(trangThai);

        // Tìm kiếm + lọc theo trạng thái
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (statusFilter != null) {
                orderPage = donHangRepo.searchByKeywordAndStatuses(keyword.trim(), statusFilter, pageable);
            } else {
                orderPage = donHangRepo.searchByKeyword(keyword.trim(), pageable);
            }
        } else if (statusFilter != null) {
            orderPage = donHangRepo.findByTrangThaiIn(statusFilter, pageable);
        } else {
            orderPage = donHangRepo.findAll(pageable);
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "admin/admin-order";
    }

    /**
     * API lấy chi tiết đơn hàng
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderApi(@PathVariable Integer id) {
        DonHang dh = donHangRepo.findById(id).orElse(null);
        if (dh == null) return ResponseEntity.notFound().build();
        TaiKhoan account = dh.getTaiKhoan();
        String recipientName = dh.getTenNguoiNhan() != null && !dh.getTenNguoiNhan().isBlank()
                ? dh.getTenNguoiNhan()
                : (account != null ? account.getHoTen() : "Khách lẻ");
        String recipientPhone = dh.getSoDienThoaiNhan() != null && !dh.getSoDienThoaiNhan().isBlank()
                ? dh.getSoDienThoaiNhan()
                : (account != null ? account.getSoDienThoai() : "Chưa cập nhật");

        Map<String, Object> response = new HashMap<>();
        response.put("maDonHang", dh.getMaDonHang());
        response.put("tenNguoiNhan", recipientName);
        response.put("sdt", recipientPhone);
        response.put("diaChi", dh.getDiaChiGiaoHang());
        response.put("ghiChu", dh.getGhiChu() != null && !dh.getGhiChu().isBlank() ? dh.getGhiChu() : "Không có ghi chú");
        response.put("tongTien", dh.getTongTien());
        response.put("trangThai", toDisplayOrderStatus(dh.getTrangThai()));
        response.put("trangThaiThanhToan", toDisplayPaymentStatus(dh.getTrangThaiThanhToan()));
        response.put("ngayDat", dh.getNgayDat().toString());
        response.put("phiVanChuyen", dh.getPhiVanChuyen());
        response.put("tienGiamGia", dh.getTienGiamGia());

        List<Map<String, Object>> products = new ArrayList<>();
        for (ChiTietDonHang ct : chiTietRepo.findByDonHang_MaDonHang(id)) {
            Map<String, Object> item = new HashMap<>();
            item.put("tenSp", ct.getBienThe().getMoHinh().getTenMoHinh());
            item.put("phanLoai", ct.getBienThe().getKichThuoc());
            item.put("soLuong", ct.getSoLuong());
            item.put("donGia", ct.getDonGia());
            item.put("hinhAnh", ct.getBienThe().getMoHinh().getHinhAnh());
            item.put("thanhTien", ct.getSoLuong() * ct.getDonGia());
            products.add(item);
        }
        response.put("products", products);

        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/update")
    @PreAuthorize("@adminPermissionGuard.hasPermission('ORDER_CONFIRM') or @adminPermissionGuard.hasPermission('ORDER_CANCEL')")
    public String updateStatus(@RequestParam("maDonHang") Integer id,
                               @RequestParam("trangThai") String trangThai,
                               @RequestParam("trangThaiThanhToan") String trangThaiThanhToan,
                               RedirectAttributes params) {
        try {
            DonHang dh = donHangRepo.findById(id).orElseThrow();
            String storedOrderStatus = toStoredOrderStatus(trangThai);
            String storedPaymentStatus = toStoredPaymentStatus(trangThaiThanhToan);
            boolean isCancelAction = "Đã hủy".equalsIgnoreCase(storedOrderStatus);
            if (isCancelAction && !hasPermission(RolePermission.ORDER_CANCEL.getCode())) {
                params.addFlashAttribute("message", "Bạn không có quyền hủy đơn hàng.");
                params.addFlashAttribute("messageType", "error");
                return "redirect:/admin/orders";
            }
            if (!isCancelAction && !hasPermission(RolePermission.ORDER_CONFIRM.getCode())) {
                params.addFlashAttribute("message", "Bạn không có quyền xác nhận hoặc cập nhật trạng thái đơn hàng.");
                params.addFlashAttribute("messageType", "error");
                return "redirect:/admin/orders";
            }

            boolean shippingOrDelivered = "Đang giao".equalsIgnoreCase(storedOrderStatus)
                    || "Hoàn thành".equalsIgnoreCase(storedOrderStatus);
            if (shippingOrDelivered && !"Đã thanh toán".equalsIgnoreCase(storedPaymentStatus)) {
                params.addFlashAttribute("message", "Chỉ nên bắt đầu giao hàng sau khi đã xác nhận nhận tiền.");
                params.addFlashAttribute("messageType", "error");
                return "redirect:/admin/orders";
            }

            String effectivePaymentStatus = isCancelAction && !hasPermission(RolePermission.ORDER_CONFIRM.getCode())
                    ? dh.getTrangThaiThanhToan()
                    : storedPaymentStatus;
            boolean shouldConfirmPayment = !isPaid(dh.getTrangThaiThanhToan()) && isPaid(effectivePaymentStatus);

            if (shouldConfirmPayment) {
                orderService.confirmPayment(id);
                dh = donHangRepo.findById(id).orElseThrow();
            } else {
                dh.setTrangThaiThanhToan(effectivePaymentStatus);
            }

            dh.setTrangThai(storedOrderStatus);
            donHangRepo.save(dh);
            params.addFlashAttribute("message", "Cập nhật đơn hàng #" + id + " thành công!");
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/orders";
    }

    /**
     * Xóa đơn hàng
     */
    @GetMapping("/delete/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('ORDER_DELETE')")
    public String delete(@PathVariable Integer id, RedirectAttributes params) {
        try {
            List<ChiTietDonHang> details = chiTietRepo.findByDonHang_MaDonHang(id);
            chiTietRepo.deleteAll(details);

            donHangRepo.deleteById(id);
            params.addFlashAttribute("message", "Đã xóa đơn hàng #" + id);
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Không thể xóa đơn hàng này!");
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/orders";
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/cancel/{id}")
    @PreAuthorize("@adminPermissionGuard.hasPermission('ORDER_CANCEL')")
    public String cancel(@PathVariable Integer id, RedirectAttributes params) {
        try {
            DonHang dh = donHangRepo.findById(id).orElseThrow();
            dh.setTrangThai("Đã hủy");
            donHangRepo.save(dh);
            params.addFlashAttribute("message", "Đã hủy đơn hàng #" + id);
            params.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            params.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            params.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/orders";
    }

    private boolean hasPermission(String permissionCode) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        return authentication != null
                && authentication.getAuthorities() != null
                && authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(com.example.asm.security.SecurityAuthorityUtils.permissionAuthority(permissionCode)));
    }

    /**
     * Thống kê đơn hàng theo trạng thái
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getOrderStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("datHang", donHangRepo.countByTrangThaiIn(ORDER_GROUP_NEW));
        stats.put("dangGiao", donHangRepo.countByTrangThaiIn(ORDER_GROUP_SHIPPING));
        stats.put("daGiao", donHangRepo.countByTrangThaiIn(ORDER_GROUP_DELIVERED));
        stats.put("daHuy", donHangRepo.countByTrangThai("Đã hủy"));
        stats.put("tongDon", donHangRepo.count());
        return ResponseEntity.ok(stats);
    }

    private List<String> resolveStatusFilter(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        return switch (status) {
            case "Đặt hàng" -> ORDER_GROUP_NEW;
            case "Đang giao" -> ORDER_GROUP_SHIPPING;
            case "Đã giao" -> ORDER_GROUP_DELIVERED;
            case "Đã hủy" -> ORDER_GROUP_CANCELLED;
            default -> List.of(status);
        };
    }

    private String toStoredOrderStatus(String displayStatus) {
        if (displayStatus == null || displayStatus.isBlank()) {
            return "Chờ xử lý";
        }
        return switch (displayStatus) {
            case "Đặt hàng" -> "Chờ xử lý";
            case "Đã giao" -> "Hoàn thành";
            default -> displayStatus;
        };
    }

    private String toStoredPaymentStatus(String displayStatus) {
        if ("Đã nhận tiền".equalsIgnoreCase(displayStatus) || "Đã thanh toán".equalsIgnoreCase(displayStatus)) {
            return "Đã thanh toán";
        }
        return "Chờ thanh toán";
    }

    private String toDisplayOrderStatus(String storedStatus) {
        if (storedStatus == null || storedStatus.isBlank()) {
            return "Đặt hàng";
        }
        if (ORDER_GROUP_NEW.stream().anyMatch(status -> status.equalsIgnoreCase(storedStatus))) {
            return "Đặt hàng";
        }
        if (ORDER_GROUP_SHIPPING.stream().anyMatch(status -> status.equalsIgnoreCase(storedStatus))) {
            return "Đang giao";
        }
        if (ORDER_GROUP_DELIVERED.stream().anyMatch(status -> status.equalsIgnoreCase(storedStatus))) {
            return "Đã giao";
        }
        return storedStatus;
    }

    private String toDisplayPaymentStatus(String storedStatus) {
        return "Đã thanh toán".equalsIgnoreCase(storedStatus) || "Đã nhận tiền".equalsIgnoreCase(storedStatus)
                ? "Đã nhận tiền"
                : "Chưa nhận tiền";
    }

    private boolean isPaid(String status) {
        return "Đã thanh toán".equalsIgnoreCase(status) || "Đã nhận tiền".equalsIgnoreCase(status);
    }
}
