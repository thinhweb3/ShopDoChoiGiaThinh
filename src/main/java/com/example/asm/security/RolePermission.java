package com.example.asm.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public enum RolePermission {

    CATEGORY_CREATE("CATEGORY_CREATE", "Danh mục", "Thêm danh mục", "Tạo danh mục mới"),
    CATEGORY_UPDATE("CATEGORY_UPDATE", "Danh mục", "Sửa danh mục", "Chỉnh sửa tên và mô tả danh mục"),
    CATEGORY_DELETE("CATEGORY_DELETE", "Danh mục", "Xóa danh mục", "Xóa danh mục không còn sử dụng"),

    MANUFACTURER_CREATE("MANUFACTURER_CREATE", "Hãng sản xuất", "Thêm hãng", "Tạo hãng sản xuất mới"),
    MANUFACTURER_UPDATE("MANUFACTURER_UPDATE", "Hãng sản xuất", "Sửa hãng", "Chỉnh sửa thông tin hãng"),
    MANUFACTURER_DELETE("MANUFACTURER_DELETE", "Hãng sản xuất", "Xóa hãng", "Xóa hãng sản xuất"),

    PRODUCT_CREATE("PRODUCT_CREATE", "Sản phẩm", "Thêm sản phẩm", "Tạo sản phẩm mới"),
    PRODUCT_UPDATE("PRODUCT_UPDATE", "Sản phẩm", "Sửa sản phẩm", "Cập nhật thông tin sản phẩm"),
    PRODUCT_DELETE("PRODUCT_DELETE", "Sản phẩm", "Xóa sản phẩm", "Xóa sản phẩm khỏi hệ thống"),
    PRODUCT_VARIANT_MANAGE("PRODUCT_VARIANT_MANAGE", "Sản phẩm", "Quản lý tồn kho", "Đồng bộ giá bán và tồn kho sản phẩm"),

    ORDER_CONFIRM("ORDER_CONFIRM", "Đơn hàng", "Xác nhận đơn", "Cập nhật xử lý, giao hàng, hoàn thành"),
    ORDER_CANCEL("ORDER_CANCEL", "Đơn hàng", "Hủy đơn", "Đổi trạng thái đơn sang đã hủy"),
    ORDER_DELETE("ORDER_DELETE", "Đơn hàng", "Xóa đơn", "Xóa đơn hàng khỏi hệ thống"),

    ACCOUNT_CREATE("ACCOUNT_CREATE", "Tài khoản", "Thêm tài khoản", "Tạo tài khoản quản trị hoặc nhân viên"),
    ACCOUNT_UPDATE("ACCOUNT_UPDATE", "Tài khoản", "Sửa tài khoản", "Chỉnh sửa thông tin tài khoản"),
    ACCOUNT_LOCK("ACCOUNT_LOCK", "Tài khoản", "Khóa mở tài khoản", "Khóa hoặc mở khóa tài khoản"),
    ACCOUNT_DELETE("ACCOUNT_DELETE", "Tài khoản", "Xóa tài khoản", "Xóa tài khoản không còn dùng"),

    ROLE_CREATE("ROLE_CREATE", "Vai trò", "Thêm vai trò", "Tạo vai trò mới"),
    ROLE_UPDATE("ROLE_UPDATE", "Vai trò", "Sửa vai trò", "Chỉnh sửa tên, mô tả và quyền"),
    ROLE_DELETE("ROLE_DELETE", "Vai trò", "Xóa vai trò", "Xóa vai trò không còn gán"),

    PROMOTION_CREATE("PROMOTION_CREATE", "Khuyến mãi", "Thêm mã giảm giá", "Tạo khuyến mãi mới"),
    PROMOTION_UPDATE("PROMOTION_UPDATE", "Khuyến mãi", "Sửa mã giảm giá", "Cập nhật nội dung khuyến mãi"),
    PROMOTION_DELETE("PROMOTION_DELETE", "Khuyến mãi", "Xóa mã giảm giá", "Xóa mã khuyến mãi"),

    INBOUND_CREATE("INBOUND_CREATE", "Nhập hàng", "Tạo đơn nhập", "Tạo đơn nhập hàng mới"),
    INBOUND_UPDATE("INBOUND_UPDATE", "Nhập hàng", "Sửa đơn nhập", "Cập nhật ghi chú và chi tiết nhập"),
    INBOUND_DELETE("INBOUND_DELETE", "Nhập hàng", "Xóa đơn nhập", "Xóa đơn nhập hoặc chi tiết nhập");

    private static final Map<String, RolePermission> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toMap(RolePermission::getCode, permission -> permission));

    private final String code;
    private final String group;
    private final String label;
    private final String description;

    RolePermission(String code, String group, String label, String description) {
        this.code = code;
        this.group = group;
        this.label = label;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getGroup() {
        return group;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    public static boolean isValid(String code) {
        String normalized = normalizeCode(code);
        return normalized != null && BY_CODE.containsKey(normalized);
    }

    public static RolePermission fromCode(String code) {
        String normalized = normalizeCode(code);
        return normalized == null ? null : BY_CODE.get(normalized);
    }

    public static List<RolePermission> orderedValues() {
        return List.of(values());
    }

    public static Map<String, List<RolePermission>> groupedOptions() {
        LinkedHashMap<String, List<RolePermission>> grouped = new LinkedHashMap<>();
        for (RolePermission permission : values()) {
            grouped.computeIfAbsent(permission.getGroup(), ignored -> new java.util.ArrayList<>()).add(permission);
        }
        return grouped;
    }

    public static LinkedHashSet<String> normalizeCodes(Collection<String> codes) {
        if (codes == null) {
            return new LinkedHashSet<>();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String rawCode : codes) {
            String code = normalizeCode(rawCode);
            if (code != null && BY_CODE.containsKey(code)) {
                normalized.add(code);
            }
        }
        return normalized;
    }

    public static LinkedHashSet<String> allCodes() {
        return orderedValues().stream()
                .map(RolePermission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static LinkedHashSet<String> adminDefaultCodes() {
        return orderedValues().stream()
                .filter(permission -> !permission.name().startsWith("ROLE_"))
                .map(RolePermission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static List<String> labelsFor(Collection<String> codes) {
        return normalizeCodes(codes).stream()
                .map(RolePermission::fromCode)
                .filter(Objects::nonNull)
                .map(RolePermission::getLabel)
                .toList();
    }

    public static Set<String> codesForGroup(String group) {
        return orderedValues().stream()
                .filter(permission -> Objects.equals(permission.getGroup(), group))
                .map(RolePermission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
