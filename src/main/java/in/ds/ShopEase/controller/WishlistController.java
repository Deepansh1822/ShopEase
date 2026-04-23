package in.ds.ShopEase.controller;

import in.ds.ShopEase.model.Product;
import in.ds.ShopEase.model.User;
import in.ds.ShopEase.model.Wishlist;
import in.ds.ShopEase.repository.ProductRepository;
import in.ds.ShopEase.repository.UserRepository;
import in.ds.ShopEase.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/wishlist")
    public String viewWishlist(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        User user = userRepository.findByEmail(principal.getName());
        List<Wishlist> wishlistItems = wishlistRepository.findByUserOrderByAddedAtDesc(user);
        model.addAttribute("wishlistItems", wishlistItems);
        return "wishlist";
    }

    @GetMapping("/wishlist/toggle/{id}")
    @ResponseBody
    public String toggleWishlist(@PathVariable Long id, Principal principal) {
        if (principal == null) return "error_auth";
        
        User user = userRepository.findByEmail(principal.getName());
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return "error_product";

        Optional<Wishlist> existing = wishlistRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return "removed";
        } else {
            wishlistRepository.save(new Wishlist(user, product));
            return "added";
        }
    }

    @GetMapping("/wishlist/add/{id}")
    public String addToWishlist(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes, @RequestParam(required = false) String redirect) {
        if (principal == null) return "redirect:/login";
        
        User user = userRepository.findByEmail(principal.getName());
        Product product = productRepository.findById(id).orElse(null);
        
        if (product != null) {
            Optional<Wishlist> existing = wishlistRepository.findByUserAndProduct(user, product);
            if (existing.isEmpty()) {
                wishlistRepository.save(new Wishlist(user, product));
                redirectAttributes.addFlashAttribute("wishlistSuccess", "Product added to wishlist!");
            }
        }
        
        if (redirect != null && !redirect.isEmpty()) return "redirect:" + redirect;
        return "redirect:/wishlist";
    }

    @GetMapping("/wishlist/remove/{id}")
    public String removeFromWishlist(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        User user = userRepository.findByEmail(principal.getName());
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            wishlistRepository.findByUserAndProduct(user, product).ifPresent(w -> wishlistRepository.delete(w));
        }
        return "redirect:/wishlist";
    }
}
