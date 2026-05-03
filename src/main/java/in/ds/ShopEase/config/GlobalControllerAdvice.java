package in.ds.ShopEase.config;

import in.ds.ShopEase.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @Autowired
    private in.ds.ShopEase.repository.UserRepository userRepository;

    @ModelAttribute("cartCount")
    public int getCartCount() {
        return cartService.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
    }

    @ModelAttribute("isMember")
    public boolean isMember(org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            in.ds.ShopEase.model.User user = userRepository.findByEmail(authentication.getName());
            if (user != null) {
                boolean isValid = user.isMember() && user.getMembershipExpiryDate() != null 
                                && java.time.LocalDateTime.now().isBefore(user.getMembershipExpiryDate());
                
                if (user.isMember() && !isValid) {
                    user.setMember(false);
                    userRepository.save(user);
                }
                
                cartService.setMember(isValid);
                return isValid;
            }
        }
        cartService.setMember(false);
        return false;
    }

    @ModelAttribute("membershipExpiry")
    public java.time.LocalDateTime membershipExpiry(org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            in.ds.ShopEase.model.User user = userRepository.findByEmail(authentication.getName());
            return (user != null && user.isMember()) ? user.getMembershipExpiryDate() : null;
        }
        return null;
    }

    @ModelAttribute("membershipPlan")
    public String membershipPlan(org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            in.ds.ShopEase.model.User user = userRepository.findByEmail(authentication.getName());
            return (user != null && user.isMember()) ? user.getMembershipPlan() : "No Membership";
        }
        return "No Membership";
    }

    @ModelAttribute("userFirstName")
    public String userFirstName(org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            in.ds.ShopEase.model.User user = userRepository.findByEmail(authentication.getName());
            return user != null ? user.getFirstName() : null;
        }
        return null;
    }
}
