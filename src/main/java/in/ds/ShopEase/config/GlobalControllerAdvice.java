package in.ds.ShopEase.config;

import in.ds.ShopEase.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @ModelAttribute("cartCount")
    public int getCartCount() {
        return cartService.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
    }
}
