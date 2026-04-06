package com.example.asm.service;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.GioHang;
import com.example.asm.entity.TaiKhoan;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.GioHangRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {
    private static final String GUEST_CART_SESSION_KEY = "guestCart";

    @Autowired GioHangRepository gioHangRepo;
    @Autowired BienTheMoHinhRepository bienTheRepo;
    @Autowired HttpSession session;

    public List<GioHang> getCart(TaiKhoan user) {
        if (user == null) {
            return getGuestCart();
        }
        return gioHangRepo.findByTaiKhoan_MaTaiKhoan(user.getMaTaiKhoan());
    }

    public void add(TaiKhoan user, Integer maBienThe, int soLuong) {
        if (soLuong <= 0) {
            return;
        }

        BienTheMoHinh bt = bienTheRepo.findById(maBienThe)
                .orElseThrow(() -> new RuntimeException("Đồ chơi không tồn tại"));

        if (user == null) {
            Map<Integer, Integer> guestCart = getGuestCartMap();
            int currentQuantity = guestCart.getOrDefault(maBienThe, 0);
            ensureEnoughStock(bt, currentQuantity + soLuong);
            guestCart.put(maBienThe, currentQuantity + soLuong);
            session.setAttribute(GUEST_CART_SESSION_KEY, guestCart);
            return;
        }

        Optional<GioHang> optItem = gioHangRepo.findByTaiKhoan_MaTaiKhoanAndBienThe_MaBienThe(user.getMaTaiKhoan(), maBienThe);

        GioHang item;
        if (optItem.isPresent()) {
            item = optItem.get();
            ensureEnoughStock(bt, item.getSoLuong() + soLuong);
            item.setSoLuong(item.getSoLuong() + soLuong);
            item.setNgayCapNhat(LocalDateTime.now());
        } else {
            ensureEnoughStock(bt, soLuong);
            item = GioHang.builder()
                    .taiKhoan(user)
                    .bienThe(bt)
                    .soLuong(soLuong)
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
        }
        gioHangRepo.save(item);
    }

    public void update(TaiKhoan user, Integer itemKey, int qty) {
        if (user == null) {
            Map<Integer, Integer> guestCart = getGuestCartMap();
            if (qty > 0) {
                BienTheMoHinh bienThe = bienTheRepo.findById(itemKey)
                        .orElseThrow(() -> new RuntimeException("Đồ chơi không tồn tại"));
                ensureEnoughStock(bienThe, qty);
                guestCart.put(itemKey, qty);
            } else {
                guestCart.remove(itemKey);
            }
            session.setAttribute(GUEST_CART_SESSION_KEY, guestCart);
            return;
        }

        GioHang item = gioHangRepo.findByTaiKhoan_MaTaiKhoanAndBienThe_MaBienThe(user.getMaTaiKhoan(), itemKey).orElse(null);
        if (item == null) {
            return;
        }

        if (qty > 0) {
            ensureEnoughStock(item.getBienThe(), qty);
            item.setSoLuong(qty);
            item.setNgayCapNhat(LocalDateTime.now());
            gioHangRepo.save(item);
        } else {
            gioHangRepo.deleteById(item.getMaGioHang());
        }
    }

    public void remove(TaiKhoan user, Integer itemKey) {
        if (user == null) {
            Map<Integer, Integer> guestCart = getGuestCartMap();
            guestCart.remove(itemKey);
            session.setAttribute(GUEST_CART_SESSION_KEY, guestCart);
            return;
        }

        gioHangRepo.findByTaiKhoan_MaTaiKhoanAndBienThe_MaBienThe(user.getMaTaiKhoan(), itemKey)
                .ifPresent(item -> gioHangRepo.deleteById(item.getMaGioHang()));
    }

    @Transactional
    public void clear(TaiKhoan user) {
        if (user == null) {
            session.removeAttribute(GUEST_CART_SESSION_KEY);
            return;
        }
        gioHangRepo.deleteByTaiKhoan_MaTaiKhoan(user.getMaTaiKhoan());
    }

    public long getAmount(TaiKhoan user) {
        return getCart(user).stream()
               .mapToLong(i -> i.getSoLuong() * i.getBienThe().getGiaBan())
               .sum();
    }
    
    public int getCount(TaiKhoan user) {
        return getCart(user).size();
    }

    private void ensureEnoughStock(BienTheMoHinh bienThe, int quantity) {
        int available = bienThe.getSoLuongTon() == null ? 0 : bienThe.getSoLuongTon();
        if (available < quantity) {
            throw new RuntimeException("Kho không đủ hàng!");
        }
    }

    private List<GioHang> getGuestCart() {
        Map<Integer, Integer> guestCart = getGuestCartMap();
        List<GioHang> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : guestCart.entrySet()) {
            BienTheMoHinh bienThe = bienTheRepo.findById(entry.getKey()).orElse(null);
            Integer quantity = entry.getValue();
            if (bienThe == null || quantity == null || quantity <= 0) {
                continue;
            }
            items.add(GioHang.builder()
                    .maGioHang(entry.getKey())
                    .bienThe(bienThe)
                    .soLuong(quantity)
                    .ngayCapNhat(LocalDateTime.now())
                    .build());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> getGuestCartMap() {
        Object raw = session.getAttribute(GUEST_CART_SESSION_KEY);
        if (raw instanceof Map<?, ?> existingMap) {
            Map<Integer, Integer> cart = new LinkedHashMap<>();
            existingMap.forEach((key, value) -> {
                if (key instanceof Integer integerKey && value instanceof Integer integerValue) {
                    cart.put(integerKey, integerValue);
                }
            });
            return cart;
        }
        return new LinkedHashMap<>();
    }
}
