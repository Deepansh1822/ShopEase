package in.ds.ShopEase.config;

import in.ds.ShopEase.model.Category;
import in.ds.ShopEase.model.Product;
import in.ds.ShopEase.model.Role;
import in.ds.ShopEase.model.User;
import in.ds.ShopEase.repository.CategoryRepository;
import in.ds.ShopEase.repository.ProductRepository;
import in.ds.ShopEase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private in.ds.ShopEase.repository.FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // ── Auto-seed Admin Account ────────────────────────────────────────────────
        if (userRepository.findByEmail("admin@shopease.com") == null) {
            User admin = new User();
            admin.setFirstName("ShopEase");
            admin.setLastName("Admin");
            admin.setEmail("admin@shopease.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRoles(Arrays.asList(new Role("ROLE_ADMIN")));
            userRepository.save(admin);
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║  Admin Account Auto-Created!                 ║");
            System.out.println("║  Email   : admin@shopease.com                ║");
            System.out.println("║  Password: Admin@123                         ║");
            System.out.println("╚══════════════════════════════════════════════╝");
        }

        // ── Seed Product Catalogue ─────────────────────────────────────────────────
        if (categoryRepository.count() == 0) {
            Category electronics = new Category();
            electronics.setName("Electronics");
            categoryRepository.save(electronics);

            Category fashion = new Category();
            fashion.setName("Fashion");
            categoryRepository.save(fashion);

            Category homeLiving = new Category();
            homeLiving.setName("Home & Living");
            categoryRepository.save(homeLiving);

            Category appliances = new Category();
            appliances.setName("Appliances");
            categoryRepository.save(appliances);

            // Electronics
            productRepository.save(new Product(null, "Wireless Headphones", "Sony", "Premium noise cancelling headphones", 2999.0, "headphones.png", electronics));
            productRepository.save(new Product(null, "Smart Watch", "Apple", "Fitness tracking and notifications", 1999.0, "watch.png", electronics));
            productRepository.save(new Product(null, "Flagship Smartphone", "Samsung", "Latest model with triple camera", 59999.0, "smartphone.png", electronics));
            productRepository.save(new Product(null, "Pro Laptop", "Dell", "Powerful ultrathin laptop for creators", 89999.0, "laptop.png", electronics));

            // Fashion
            productRepository.save(new Product(null, "Cotton T-Shirt", "Zara", "Premium quality 100% cotton", 999.0, "tshirt.png", fashion));
            productRepository.save(new Product(null, "Leather Jacket", "Levis", "Stylish dark brown leather jacket", 4999.0, "jacket.png", fashion));
            productRepository.save(new Product(null, "Urban Sneakers", "Nike", "Comfortable white trendy sneakers", 2499.0, "sneakers.png", fashion));

            // Home & Living
            productRepository.save(new Product(null, "Modern Sofa", "IKEA", "Minimalist grey 3-seater sofa", 15999.0, "sofa.png", homeLiving));

            // Appliances
            productRepository.save(new Product(null, "Espresso Machine", "Breville", "Professional coffee maker", 7999.0, "coffeemaker.png", appliances));

            // Seed Feedbacks
            feedbackRepository.save(new in.ds.ShopEase.model.Feedback("Sarah Khan", "Verified Buyer", "The best e-commerce experience I've ever had. Products are top-notch and delivery was incredibly fast!", 5));
            feedbackRepository.save(new in.ds.ShopEase.model.Feedback("Rahul Jain", "Tech Enthusiast", "Absolutely love the UI of this website! Makes shopping so easy and seamless. Payment via Razorpay worked flawlessly.", 5));
            feedbackRepository.save(new in.ds.ShopEase.model.Feedback("Anita Morris", "Retail Manager", "Their customer support is amazing, and the products always arrive in premium packaging. Highly recommended.", 4));
        }
    }
}
