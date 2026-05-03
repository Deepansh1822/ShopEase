package in.ds.ShopEase.service;

import in.ds.ShopEase.model.CartItem;
import in.ds.ShopEase.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {
    private List<CartItem> items = new ArrayList<>();
    private String appliedPromoCode = null;
    private double discountPercentage = 0.0;

    private boolean isMember = false;

    public void setMember(boolean member) {
        this.isMember = member;
    }

    public boolean isMember() {
        return isMember;
    }

    public void addItem(Product product) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                if (item.getQuantity() + 1 > product.getStockQuantity()) {
                    throw new IllegalStateException("Sorry, only " + product.getStockQuantity() + " units are available in stock.");
                }
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        if (product.getStockQuantity() < 1) {
            throw new IllegalStateException("Sorry, this product is currently out of stock.");
        }
        items.add(new CartItem(product, 1));
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
    }

    public double getMembershipDiscount() {
        return isMember ? getSubtotal() * 0.10 : 0.0; // 10% discount for members
    }

    public double getPromoDiscountAmount() {
        return (getSubtotal() - getMembershipDiscount()) * (discountPercentage / 100.0);
    }

    public double getDiscountAmount() {
        return getMembershipDiscount() + getPromoDiscountAmount();
    }

    public double getDeliveryFee() {
        if (isMember) return 0.0; // Free delivery for members
        double currentTotal = getSubtotal() - getDiscountAmount();
        if (currentTotal == 0) return 0.0;
        return (currentTotal >= 999) ? 0.0 : 100.0;
    }

    public double getTotal() {
        return getSubtotal() - getDiscountAmount() + getDeliveryFee();
    }

    public void applyPromo(String code) {
        if ("SUMMER50".equalsIgnoreCase(code) && getSubtotal() > 2999) {
            this.appliedPromoCode = code.toUpperCase();
            this.discountPercentage = 50.0;
        } else if ("WELCOME20".equalsIgnoreCase(code)) {
            this.appliedPromoCode = code.toUpperCase();
            this.discountPercentage = 20.0;
        } else {
            throw new IllegalArgumentException("Invalid promo code or minimum conditions not met.");
        }
    }

    public void removePromo() {
        this.appliedPromoCode = null;
        this.discountPercentage = 0.0;
    }

    public String getAppliedPromoCode() {
        return appliedPromoCode;
    }

    public void updateQuantity(Long productId, int change) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                int newQty = item.getQuantity() + change;
                if (newQty >= 1) {
                    if (newQty > item.getProduct().getStockQuantity()) {
                        throw new IllegalStateException("Only " + item.getProduct().getStockQuantity() + " units available.");
                    }
                    item.setQuantity(newQty);
                }
                return;
            }
        }
    }

    public void clear() {
        items.clear();
        removePromo();
    }
}
