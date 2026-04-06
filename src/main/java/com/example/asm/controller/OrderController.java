package com.example.asm.controller;

import com.example.asm.entity.DonHang;
import com.example.asm.entity.KhuyenMai;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.KhuyenMaiRepository;
import com.example.asm.service.AuthService;
import com.example.asm.service.CartService;
import com.example.asm.service.OrderDetailService;
import com.example.asm.service.OrderService;
import com.example.asm.service.PaymentInboxService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired CartService cartService;
    @Autowired AuthService authService;
    @Autowired OrderService orderService;
    @Autowired OrderDetailService orderDetailService;
    @Autowired PaymentInboxService paymentInboxService;
    @Autowired KhuyenMaiRepository kmRepo;

    @Value("${order.shipping-fee:30000}") long shippingFee;
    @Value("${payment.qr.bank-id:970422}") String qrBankId;
    @Value("${payment.qr.account:1234567890}") String qrAccount;
    @Value("${payment.qr.acq-name:}") String qrAcqName;
    @Value("${payment.qr.template:compact2}") String qrTemplate;

    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam("voucherCode") String code, HttpSession session, RedirectAttributes params) {
        TaiKhoan user = authService.getUser();
        long amount = cartService.getAmount(user);

        KhuyenMai km = kmRepo.findById(code).orElse(null);

        if (km == null || !km.getTrangThai()) {
            params.addFlashAttribute("voucherError", "Mã không tồn tại hoặc đã bị khóa!");
            session.removeAttribute("voucher");
            return "redirect:/order/checkout";
        }
        if (km.getSoLuong() <= 0) {
            params.addFlashAttribute("voucherError", "Mã giảm giá đã hết lượt sử dụng!");
            session.removeAttribute("voucher");
            return "redirect:/order/checkout";
        }
        if (LocalDateTime.now().isBefore(km.getNgayBatDau()) || LocalDateTime.now().isAfter(km.getNgayKetThuc())) {
            params.addFlashAttribute("voucherError", "Mã giảm giá chưa bắt đầu hoặc đã hết hạn!");
            session.removeAttribute("voucher");
            return "redirect:/order/checkout";
        }
        if (amount < km.getDonToiThieu()) {
            params.addFlashAttribute("voucherError", "Đơn hàng phải từ " + km.getDonToiThieu() + "đ mới được dùng mã này!");
            session.removeAttribute("voucher");
            return "redirect:/order/checkout";
        }

        session.setAttribute("voucher", km);
        params.addFlashAttribute("voucherSuccess", "Áp dụng mã " + code + " thành công!");
        return "redirect:/order/checkout";
    }

    @GetMapping("/remove-voucher")
    public String removeVoucher(HttpSession session) {
        session.removeAttribute("voucher");
        return "redirect:/order/checkout";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        TaiKhoan user = authService.getUser();
        
        if (cartService.getCount(user) == 0) return "redirect:/home/index";

        long originalAmount = cartService.getAmount(user);
        long discountAmount = 0;

        KhuyenMai km = (KhuyenMai) session.getAttribute("voucher");
        if (km != null) {
            if(km.getPhanTramGiam() != null && km.getPhanTramGiam() > 0) {
                discountAmount = originalAmount * km.getPhanTramGiam() / 100;
                if(km.getGiamToiDa() != null && discountAmount > km.getGiamToiDa()) {
                    discountAmount = km.getGiamToiDa();
                }
            } else if (km.getSoTienGiam() != null) {
                discountAmount = km.getSoTienGiam();
            }
        }

        long finalAmount = originalAmount - discountAmount;
        if(finalAmount < 0) finalAmount = 0;
        long payableAmount = finalAmount + shippingFee;

        model.addAttribute("cart", cartService.getCart(user));
        model.addAttribute("amount", finalAmount); 
        model.addAttribute("originalAmount", originalAmount); 
        model.addAttribute("discountAmount", discountAmount); 
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("payableAmount", payableAmount);
        
        return "fragments/checkout";
    }


    public String purchase(@RequestParam("address") String address,
                           @RequestParam("note") String note,
                           @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
                           HttpSession session,
                           RedirectAttributes params) {
        try {
            if (address != null && address.matches(".*[+\\-×÷&%^*()].*")) {
                params.addFlashAttribute("message", "Địa chỉ nhận hàng không được chứa ký tự đặc biệt");
                return "redirect:/order/checkout";
            }

            TaiKhoan user = authService.getUser();
            KhuyenMai voucher = (KhuyenMai) session.getAttribute("voucher");
            DonHang order = orderService.placeOrder(user, address, note, voucher);

            session.removeAttribute("voucher");
            session.setAttribute("recentOrderId", order.getMaDonHang());
            session.removeAttribute("guestOrderId");

            if ("CHUYEN_KHOAN".equalsIgnoreCase(paymentMethod)) {
                return "redirect:/order/payment-qr/" + order.getMaDonHang();
            }
            return "redirect:/order/success";
        } catch (Exception e) {
            params.addFlashAttribute("message", e.getMessage());
            return "redirect:/order/checkout";
        }
    }

    @PostMapping("/purchase")
    public String purchase(@RequestParam("recipientName") String recipientName,
                           @RequestParam("recipientPhone") String recipientPhone,
                           @RequestParam(value = "recipientEmail", required = false) String recipientEmail,
                           @RequestParam("address") String address,
                           @RequestParam("note") String note,
                           @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
                           HttpSession session, 
                           RedirectAttributes params) {

        try {
            if (recipientName == null || recipientName.trim().isBlank()) {
                params.addFlashAttribute("message", "Vui lòng nhập tên người nhận.");
                return "redirect:/order/checkout";
            }
            if (recipientPhone == null || recipientPhone.trim().isBlank()) {
                params.addFlashAttribute("message", "Vui lòng nhập số điện thoại người nhận.");
                return "redirect:/order/checkout";
            }
            if (address != null && address.matches(".*[+\\-×÷&%^*()].*")) {
                params.addFlashAttribute("message", "Địa chỉ nhận hàng không được chứa ký tự đặc biệt");
                return "redirect:/order/checkout";
            }
            
            TaiKhoan user = authService.getUser();
            KhuyenMai voucher = (KhuyenMai) session.getAttribute("voucher");
            
            DonHang order = orderService.placeOrder(
                    user,
                    address,
                    note,
                    voucher,
                    recipientName,
                    recipientPhone,
                    recipientEmail
            );
            
            session.removeAttribute("voucher"); 
            session.setAttribute("recentOrderId", order.getMaDonHang());
            if (user == null) {
                session.setAttribute("guestOrderId", order.getMaDonHang());
            } else {
                session.removeAttribute("guestOrderId");
            }
            
            if ("CHUYEN_KHOAN".equalsIgnoreCase(paymentMethod)) {	
                return "redirect:/order/payment-qr/" + order.getMaDonHang();
            }
            return "redirect:/order/success";

        } catch (Exception e) {
            params.addFlashAttribute("message", e.getMessage());
            return "redirect:/order/checkout";
        }
    }


    @GetMapping("/payment-qr/{id}")
    public String paymentQr(@PathVariable("id") Integer id, Model model, RedirectAttributes params, HttpSession session) {
        DonHang order = orderService.findById(id);
        TaiKhoan user = authService.getUser();
        
        if (!canAccessOrder(order, user, session)) {
            params.addFlashAttribute("message", "Bạn không có quyền xem đơn hàng này.");
            return "redirect:/home/index";
        }
        if ("Đã thanh toán".equals(order.getTrangThaiThanhToan())) {
            return "redirect:/order/success";
        }
        
        String qrImageUrl = "https://img.vietqr.io/image/" + qrBankId + "-" + qrAccount + "-" + qrTemplate + ".jpg?amount=" + order.getTongTien() + "&addInfo=DH" + order.getMaDonHang();
        model.addAttribute("order", order);
        model.addAttribute("qrImageUrl", qrImageUrl);
        model.addAttribute("qrAcqName", qrAcqName);
        model.addAttribute("qrAccount", qrAccount);
        model.addAttribute("transferContent", "DH" + order.getMaDonHang());
        return "fragments/payment-qr";
    }

    public String confirmPayment(@PathVariable("id") Integer id, RedirectAttributes params) {
        try {
            TaiKhoan user = authService.getUser();
            DonHang order = orderService.findById(id);
            if (order == null || user == null || order.getTaiKhoan() == null
                    || !order.getTaiKhoan().getMaTaiKhoan().equals(user.getMaTaiKhoan())) {
                params.addFlashAttribute("message", "Đơn hàng không tồn tại hoặc không thuộc tài khoản của bạn.");
                return "redirect:/order/list";
            }
            if ("Đã thanh toán".equals(order.getTrangThaiThanhToan())) {
                return "redirect:/order/success";
            }

            var match = paymentInboxService.findMatchingPaymentEmail(order);
            if (match.isEmpty()) {
                params.addFlashAttribute("message",
                        "Chưa tìm thấy email báo có khớp với mã chuyển khoản DH" + order.getMaDonHang()
                                + " và số tiền " + order.getTongTien() + "đ. Hãy đợi email ngân hàng rồi thử lại.");
                return "redirect:/order/payment-qr/" + id;
            }

            orderService.confirmPayment(id, user.getMaTaiKhoan());
            params.addFlashAttribute("paymentSuccess", "Thanh toán chuyển khoản đã được xác nhận. " + match.get().toDisplayText());
            return "redirect:/order/success";
        } catch (Exception e) {
            params.addFlashAttribute("message", e.getMessage());
            return "redirect:/order/payment-qr/" + id;
        }
    }

    @PostMapping("/confirm-payment/{id}")
    public String confirmPayment(@PathVariable("id") Integer id, RedirectAttributes params, HttpSession session) {
        try {
            DonHang order = orderService.findById(id);
            TaiKhoan user = authService.getUser();
            if (!canAccessOrder(order, user, session)) {
                params.addFlashAttribute("message", "Đơn hàng không tồn tại hoặc không thuộc phiên mua hàng hiện tại.");
                return "redirect:/home/index";
            }
            if ("Đã thanh toán".equals(order.getTrangThaiThanhToan())) {
                return "redirect:/order/success";
            }

            var match = paymentInboxService.findMatchingPaymentEmail(order);
            if (match.isEmpty()) {
                params.addFlashAttribute("message",
                        "Chưa tìm thấy email báo có khớp với mã chuyển khoản DH" + order.getMaDonHang()
                                + " và số tiền " + order.getTongTien() + "đ. Hãy đợi email ngân hàng rồi thử lại.");
                return "redirect:/order/payment-qr/" + id;
            }

            orderService.confirmPayment(id);
            params.addFlashAttribute("paymentSuccess", "Thanh toán chuyển khoản đã được xác nhận. " + match.get().toDisplayText());
            return "redirect:/order/success";
        } catch (Exception e) {
            params.addFlashAttribute("message", e.getMessage());
            return "redirect:/order/payment-qr/" + id;
        }
    }

    @GetMapping("/success")
    public String success(Model model, HttpSession session) {
        Integer recentOrderId = (Integer) session.getAttribute("recentOrderId");
        if (recentOrderId != null) {
            model.addAttribute("order", orderService.findById(recentOrderId));
        }
        return "fragments/order-success";
    }

    @GetMapping("/list")
    public String list(Model model) {
        if (!authService.isLogin()) return "redirect:/auth/login";
        TaiKhoan user = authService.getUser();
        model.addAttribute("orders", orderService.findByUserId(user.getMaTaiKhoan()));
        return "fragments/order-list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        if (!authService.isLogin()) return "redirect:/auth/login";
        DonHang order = orderService.findById(id);
        TaiKhoan user = authService.getUser();
        if (order == null || !order.getTaiKhoan().getMaTaiKhoan().equals(user.getMaTaiKhoan())) {
             return "redirect:/order/list";
        }
        model.addAttribute("order", order);
        model.addAttribute("details", orderDetailService.findByOrderId(id));
        return "fragments/order-detail";
    }

    private boolean canAccessOrder(DonHang order, TaiKhoan user, HttpSession session) {
        if (order == null) {
            return false;
        }
        if (user != null) {
            return order.getTaiKhoan() != null
                    && order.getTaiKhoan().getMaTaiKhoan().equals(user.getMaTaiKhoan());
        }
        return session != null
                && order.getTaiKhoan() == null
                && session.getAttribute("guestOrderId") instanceof Integer guestOrderId
                && guestOrderId != null
                && guestOrderId.equals(order.getMaDonHang());
    }
}
