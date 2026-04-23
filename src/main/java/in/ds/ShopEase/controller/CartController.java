package in.ds.ShopEase.controller;

import in.ds.ShopEase.model.Product;
import in.ds.ShopEase.service.CartService;
import in.ds.ShopEase.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @GetMapping("/cart")
    public String cartGet(Model model) {
        model.addAttribute("cart", cartService.getItems());
        model.addAttribute("subtotal", cartService.getSubtotal());
        model.addAttribute("discountAmount", cartService.getDiscountAmount());
        model.addAttribute("deliveryFee", cartService.getDeliveryFee());
        model.addAttribute("total", cartService.getTotal());
        model.addAttribute("appliedPromo", cartService.getAppliedPromoCode());
        return "cart";
    }

    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable Long id) {
        Product product = productService.getProductById(id).orElse(null);
        if (product != null) {
            cartService.addItem(product);
        }
        return "redirect:/shop";
    }

    @GetMapping("/cart/removeItem/{id}")
    public String removeItem(@PathVariable Long id) {
        cartService.removeItem(id);
        return "redirect:/cart";
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/applyPromo")
    public String applyPromo(@org.springframework.web.bind.annotation.RequestParam("promoCode") String promoCode, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            cartService.applyPromo(promoCode);
            redirectAttributes.addFlashAttribute("successMsg", "Promo code applied successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/removePromo")
    public String removePromo() {
        cartService.removePromo();
        return "redirect:/cart";
    }
}
