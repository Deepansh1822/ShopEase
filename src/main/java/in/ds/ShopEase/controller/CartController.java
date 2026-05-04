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
    private in.ds.ShopEase.service.UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private in.ds.ShopEase.repository.OfferRepository offerRepository;

    @GetMapping("/cart")
    public String cartGet(Model model, java.security.Principal principal) {
        if (principal != null) {
            in.ds.ShopEase.model.User user = userService.findByEmail(principal.getName());
            if (user != null) {
                cartService.setMember(user.isMember());
            }
        }
        model.addAttribute("isMember", cartService.isMember());
        model.addAttribute("cart", cartService.getItems());
        model.addAttribute("subtotal", cartService.getSubtotal());
        model.addAttribute("membershipDiscount", cartService.getMembershipDiscount());
        model.addAttribute("promoDiscountAmount", cartService.getPromoDiscountAmount());
        model.addAttribute("discountAmount", cartService.getDiscountAmount());
        model.addAttribute("deliveryFee", cartService.getDeliveryFee());
        model.addAttribute("total", cartService.getTotal());
        model.addAttribute("appliedPromo", cartService.getAppliedPromoCode());
        return "cart";
    }

    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable Long id, java.security.Principal principal) {
        if (principal != null) {
            in.ds.ShopEase.model.User user = userService.findByEmail(principal.getName());
            if (user != null) {
                cartService.setMember(user.isMember());
            }
        }
        Product product = productService.getProductById(id).orElse(null);
        if (product != null) {
            cartService.addItem(product);
        }
        return "redirect:/shop";
    }

    @GetMapping("/api/cart/add/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> addToCartApi(@PathVariable Long id) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Product product = productService.getProductById(id).orElse(null);
            if (product != null) {
                cartService.addItem(product);
                response.put("status", "success");
                response.put("cartCount", cartService.getItems().stream().mapToInt(item -> item.getQuantity()).sum());
            } else {
                response.put("status", "error");
                response.put("message", "Product not found.");
            }
        } catch (IllegalStateException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/cart/removeItem/{id}")
    public String removeItem(@PathVariable Long id) {
        cartService.removeItem(id);
        return "redirect:/cart";
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/applyPromo")
    public String applyPromo(@org.springframework.web.bind.annotation.RequestParam("promoCode") String promoCode, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            in.ds.ShopEase.model.Offer offer = offerRepository.findByActiveTrue().stream()
                .filter(o -> promoCode.equalsIgnoreCase(o.getPromoCode()))
                .findFirst()
                .orElse(null);
                
            if (offer == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "Invalid promo code.");
                return "redirect:/cart";
            }
            
            if (offer.getExpiryDate() != null && offer.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMsg", "This promo code has expired.");
                return "redirect:/cart";
            }

            cartService.applyPromo(offer.getPromoCode(), offer.getDiscountPercentage());
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

    @GetMapping("/cart/updateQuantity/{id}/{change}")
    public String updateQuantity(@PathVariable Long id, @PathVariable int change) {
        cartService.updateQuantity(id, change);
        return "redirect:/cart";
    }

    @GetMapping("/api/cart/updateQuantity/{id}/{change}")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> updateQuantityApi(@PathVariable Long id, @PathVariable int change) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            cartService.updateQuantity(id, change);
            
            int itemQty = 0;
            double itemTotal = 0;
            for (in.ds.ShopEase.model.CartItem item : cartService.getItems()) {
                if (item.getProduct().getId().equals(id)) {
                    itemQty = item.getQuantity();
                    itemTotal = item.getProduct().getPrice() * itemQty;
                    break;
                }
            }
            
            response.put("status", "success");
            response.put("itemQty", itemQty);
            response.put("itemTotal", itemTotal);
            response.put("subtotal", cartService.getSubtotal());
            response.put("membershipDiscount", cartService.getMembershipDiscount());
            response.put("promoDiscount", cartService.getPromoDiscountAmount());
            response.put("discountAmount", cartService.getDiscountAmount());
            response.put("deliveryFee", cartService.getDeliveryFee());
            response.put("total", cartService.getTotal());
            response.put("cartCount", cartService.getItems().stream().mapToInt(i -> i.getQuantity()).sum());
        } catch (IllegalStateException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}
