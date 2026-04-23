package in.ds.ShopEase.controller;

import in.ds.ShopEase.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private in.ds.ShopEase.repository.WishlistRepository wishlistRepository;

    @Autowired
    private in.ds.ShopEase.repository.FeedbackRepository feedbackRepository;

    private java.util.Set<Long> getWishlistProductIds(java.security.Principal principal) {
        if (principal == null) return new java.util.HashSet<>();
        in.ds.ShopEase.model.User user = userRepository.findByEmail(principal.getName());
        return wishlistRepository.findByUserOrderByAddedAtDesc(user).stream()
                .map(w -> w.getProduct().getId())
                .collect(java.util.stream.Collectors.toSet());
    }

    @GetMapping({"/", "/home"})
    public String index(Model model, java.security.Principal principal) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("feedbacks", feedbackRepository.findTop9ByOrderByCreatedAtDesc());
        model.addAttribute("wishlistProductIds", getWishlistProductIds(principal));
        return "index";
    }

    @org.springframework.web.bind.annotation.GetMapping("/feedback")
    public String feedbackPage() {
        return "feedback";
    }

    @org.springframework.web.bind.annotation.PostMapping("/submit-feedback")
    public String submitFeedback(
            @org.springframework.web.bind.annotation.RequestParam("name") String name,
            @org.springframework.web.bind.annotation.RequestParam("subtitle") String subtitle,
            @org.springframework.web.bind.annotation.RequestParam("message") String message,
            @org.springframework.web.bind.annotation.RequestParam("rating") int rating,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        feedbackRepository.save(new in.ds.ShopEase.model.Feedback(name, subtitle, message, rating));
        redirectAttributes.addFlashAttribute("feedbackSuccess", "Thank you! Your feedback has been posted successfully.");
        return "redirect:/feedback";
    }

    @GetMapping("/shop")
    public String shop(Model model, java.security.Principal principal) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("wishlistProductIds", getWishlistProductIds(principal));
        return "shop";
    }

    @GetMapping("/shop/category/{id}")
    public String shopByCategory(Model model, @PathVariable Long id, java.security.Principal principal) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("wishlistProductIds", getWishlistProductIds(principal));
        return "shop";
    }

    @Autowired
    private in.ds.ShopEase.repository.UserRepository userRepository;

    @Autowired
    private in.ds.ShopEase.repository.OrderRepository orderRepository;

    @GetMapping("/offers")
    public String offersPage() {
        return "offers";
    }

    @GetMapping("/support")
    public String supportPage() {
        return "support";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, java.security.Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        in.ds.ShopEase.model.User user = userRepository.findByEmail(principal.getName());
        java.util.List<in.ds.ShopEase.model.Order> userOrders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        
        model.addAttribute("user", user);
        model.addAttribute("orders", userOrders);
        return "profile";
    }

    @Autowired
    private in.ds.ShopEase.repository.ProductReviewRepository productReviewRepository;

    @GetMapping("/shop/viewproduct/{id}")
    public String viewProduct(Model model, @PathVariable Long id, java.security.Principal principal) {
        in.ds.ShopEase.model.Product product = productService.getProductById(id).get();
        java.util.List<in.ds.ShopEase.model.ProductReview> reviews = productReviewRepository.findByProductOrderByCreatedAtDesc(product);
        
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("wishlistProductIds", getWishlistProductIds(principal));
        model.addAttribute("relatedProducts", productService.getProductsByCategoryId(product.getCategory().getId()));
        return "viewProduct";
    }

    @org.springframework.web.bind.annotation.PostMapping("/shop/submitReview")
    public String submitReview(
            @org.springframework.web.bind.annotation.RequestParam("productId") Long productId,
            @org.springframework.web.bind.annotation.RequestParam("reviewerName") String reviewerName,
            @org.springframework.web.bind.annotation.RequestParam("reviewText") String reviewText,
            @org.springframework.web.bind.annotation.RequestParam("rating") int rating,
            @org.springframework.web.bind.annotation.RequestParam(value = "reviewImage", required = false) org.springframework.web.multipart.MultipartFile file,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        in.ds.ShopEase.model.Product product = productService.getProductById(productId).orElse(null);
        if (product == null) return "redirect:/shop";

        String imageName = null;
        if (file != null && !file.isEmpty()) {
            // For now, we'll use the original filename and assume it's in /img/ (simulated)
            // In a real app, we'd save the file to the 'static/img' directory
            imageName = file.getOriginalFilename();
            try {
                String uploadDir = "src/main/resources/static/img/";
                java.nio.file.Path path = java.nio.file.Paths.get(uploadDir + imageName);
                java.nio.file.Files.copy(file.getInputStream(), path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                // Fail silently or log
            }
        }

        productReviewRepository.save(new in.ds.ShopEase.model.ProductReview(product, reviewerName, reviewText, rating, imageName));
        redirectAttributes.addFlashAttribute("reviewSuccess", "Your review has been posted!");
        return "redirect:/shop/viewproduct/" + productId;
    }
}
