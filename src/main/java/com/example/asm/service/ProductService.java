package com.example.asm.service;

import com.example.asm.entity.BienTheMoHinh;
import com.example.asm.entity.MoHinh;
import com.example.asm.repository.BienTheMoHinhRepository;
import com.example.asm.repository.MoHinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired MoHinhRepository moHinhRepo;
    @Autowired BienTheMoHinhRepository bienTheRepo;

    public List<MoHinh> findAll() {
        return moHinhRepo.findAll().stream()
                .filter(this::isVisibleForStorefront)
                .collect(Collectors.toList());
    }

    public MoHinh findById(String id) {
        return moHinhRepo.findById(id).orElse(null);
    }

    public List<MoHinh> findByCategoryId(Integer maDanhMuc) {
        return moHinhRepo.findByDanhMuc_MaDanhMuc(maDanhMuc).stream()
                .filter(this::isVisibleForStorefront)
                .collect(Collectors.toList());
    }

    public List<MoHinh> search(String keyword) {
        return moHinhRepo.findByTenMoHinhContainingIgnoreCase(keyword).stream()
                .filter(this::isVisibleForStorefront)
                .collect(Collectors.toList());
    }

    public List<MoHinh> findByFilters(Optional<Integer> categoryId, String keyword, List<String> priceFilters, String sort) {
        List<MoHinh> baseItems = categoryId
                .map(this::findByCategoryId)
                .orElseGet(this::findAll);

        String normalizedKeyword = keyword != null ? keyword.trim().toLowerCase() : "";
        List<MoHinh> filteredByKeyword = baseItems.stream()
                .filter(product -> normalizedKeyword.isEmpty()
                        || (product.getTenMoHinh() != null
                        && product.getTenMoHinh().toLowerCase().contains(normalizedKeyword))
                        || (product.getMaMoHinh() != null
                        && product.getMaMoHinh().toLowerCase().contains(normalizedKeyword)))
                .collect(Collectors.toList());

        List<MoHinh> filteredByPrice = applyPriceFilter(filteredByKeyword, priceFilters);
        applySort(filteredByPrice, sort);
        return filteredByPrice;
    }

    public List<MoHinh> findByFilters(Optional<Integer> categoryId, List<String> priceFilters) {
        return findByFilters(categoryId, null, priceFilters, "newest");
    }

    private List<MoHinh> applyPriceFilter(List<MoHinh> baseItems, List<String> priceFilters) {
        if (priceFilters == null || priceFilters.isEmpty()) {
            return baseItems;
        }

        List<MoHinh> filtered = new ArrayList<>();
        for (MoHinh product : baseItems) {
            Long minPrice = getMinPrice(product);
            boolean match = matchesAnyRange(minPrice, priceFilters);
            if (match) {
                filtered.add(product);
            }
        }
        return filtered;
    }

    private void applySort(List<MoHinh> items, String sort) {
        if (items == null || items.isEmpty()) {
            return;
        }

        String normalizedSort = sort == null ? "newest" : sort;
        switch (normalizedSort) {
            case "priceAsc":
                items.sort(Comparator.comparing(this::getMinPrice, Comparator.nullsLast(Long::compareTo)));
                break;
            case "priceDesc":
                items.sort(Comparator.comparing(this::getMinPrice, Comparator.nullsLast(Long::compareTo)).reversed());
                break;
            case "nameAsc":
                items.sort(Comparator.comparing(
                        m -> m.getTenMoHinh() == null ? "" : m.getTenMoHinh().toLowerCase()));
                break;
            case "newest":
            default:
                items.sort(Comparator.comparing(MoHinh::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
        }
    }

    public Long getMinPrice(MoHinh product) {
        if (product != null && product.getGiaBan() != null) {
            return product.getGiaBan();
        }
        if (product == null || product.getBienThes() == null || product.getBienThes().isEmpty()) {
            return null;
        }
        return product.getBienThes().stream()
                .map(BienTheMoHinh::getGiaBan)
                .filter(price -> price != null)
                .min(Long::compareTo)
                .orElse(null);
    }

    public Integer getAvailableStock(MoHinh product) {
        if (product == null) {
            return 0;
        }
        if (product.getTonKho() != null) {
            return product.getTonKho();
        }
        if (product.getBienThes() == null || product.getBienThes().isEmpty()) {
            return 0;
        }
        return product.getBienThes().stream()
                .map(BienTheMoHinh::getSoLuongTon)
                .filter(quantity -> quantity != null)
                .reduce(0, Integer::sum);
    }

    private boolean matchesAnyRange(Long price, List<String> priceFilters) {
        if (price == null) {
            return false;
        }

        for (String filter : priceFilters) {
            if ("under100".equals(filter) && price < 100_000L) {
                return true;
            }
            if ("100to300".equals(filter) && price >= 100_000L && price < 300_000L) {
                return true;
            }
            if ("300to1000".equals(filter) && price >= 300_000L && price < 1_000_000L) {
                return true;
            }
            if ("1000to2000".equals(filter) && price >= 1_000_000L && price <= 2_000_000L) {
                return true;
            }
            if ("above2000".equals(filter) && price > 2_000_000L) {
                return true;
            }
        }

        return false;
    }

    public BienTheMoHinh findVariantById(Integer id) {
        return bienTheRepo.findById(id).orElse(null);
    }

    public List<MoHinh> findTop8BestSelling() {
    	return moHinhRepo.findTop8BestSelling(PageRequest.of(0, 8));
    }
    public List<MoHinh> findOn900() {
        return moHinhRepo.findAll().stream()
                .filter(this::isVisibleForStorefront)
                .filter(product -> {
                    Long price = getMinPrice(product);
                    return price != null && price >= 900_000L;
                })
                .sorted(Comparator.comparing(MoHinh::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(8)
                .collect(Collectors.toList());
    }

    private boolean isVisibleForStorefront(MoHinh product) {
        if (product == null) {
            return false;
        }
        boolean visible = product.getTrangThai() == null || Boolean.TRUE.equals(product.getTrangThai());
        boolean allowedToSell = product.getDuocBan() == null || Boolean.TRUE.equals(product.getDuocBan());
        return visible && allowedToSell;
    }
}
