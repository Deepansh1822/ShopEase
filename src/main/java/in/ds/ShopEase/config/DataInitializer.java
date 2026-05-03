package in.ds.ShopEase.config;

import in.ds.ShopEase.model.Category;
import in.ds.ShopEase.model.Product;
import in.ds.ShopEase.model.Role;
import in.ds.ShopEase.model.User;
import in.ds.ShopEase.repository.CategoryRepository;
import in.ds.ShopEase.repository.ProductRepository;
import in.ds.ShopEase.repository.UserRepository;
import in.ds.ShopEase.repository.OfferRepository;
import in.ds.ShopEase.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");

            Role userRole = new Role();
            userRole.setName("ROLE_USER");

            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@shopease.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Arrays.asList(adminRole));

            User user = new User();
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setEmail("user@shopease.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRoles(Arrays.asList(userRole));

            userRepository.save(admin);
            userRepository.save(user);

            // Categories
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
            createProduct("Wireless Headphones", "Sony", "Premium noise cancelling headphones", 2999.0, 3999.0, 15, "headphones.png", "1 Year Manufacturer Warranty", "Industry-leading noise cancellation\nUp to 30-hour battery life\nTouch sensor controls", electronics);
            createProduct("Smart Watch", "Apple", "Fitness tracking and notifications", 19999.0, 24999.0, 8, "watch.png", "6 Months Apple Care", "Retina OLED Display\nHeart Rate Monitoring\nWater Resistant", electronics);
            createProduct("Flagship Smartphone", "Samsung", "Latest model with triple camera", 59999.0, 69999.0, 20, "smartphone.png", "1 Year Brand Warranty", "6.7-inch Dynamic AMOLED\n108MP Triple Camera\n5G Ready", electronics);

            // Fashion
            createProduct("Cotton T-Shirt", "Zara", "Premium quality 100% cotton", 999.0, 1499.0, 50, "tshirt.png", null, "100% Organic Cotton\nBreathable Fabric\nRegular Fit", fashion);
            createProduct("Urban Sneakers", "Nike", "Comfortable white trendy sneakers", 2499.0, 3499.0, 12, "sneakers.png", "6 Months Sole Warranty", "Cushioned Midsole\nRubber Outsole\nBreathable Mesh Upper", fashion);

            // Home & Living
            createProduct("Modern Sofa", "IKEA", "Minimalist grey 3-seater sofa", 15999.0, 19999.0, 5, "sofa.png", "5 Years Frame Warranty", "High-density Foam\nStain-resistant Fabric\nSolid Oak Legs", homeLiving);
            
            // Kitchen & Appliances
            createProduct("Espresso Machine", "Breville", "Professional coffee maker", 7999.0, 9999.0, 4, "coffeemaker.png", "1 Year Authentic Warranty", "15 Bar Pressure Pump\nIntegrated Grinder\nMilk Frother Wand", appliances);
            createProduct("Non-Stick Cookware Set", "Prestige", "15-piece induction base set", 3499.0, 4999.0, 25, "utensils.png", "2 Years Coating Warranty", "Greblon C3+ non-stick coating\nDishwasher safe\nPFOA Free", appliances);

            // Seed Feedbacks
            feedbackRepository.save(new in.ds.ShopEase.model.Feedback("Sarah Khan", "Verified Buyer", "The best e-commerce experience I've ever had. Products are top-notch and delivery was incredibly fast!", 5));
            feedbackRepository.save(new in.ds.ShopEase.model.Feedback("Rahul Jain", "Tech Enthusiast", "Absolutely love the UI of this website! Makes shopping so easy and seamless.", 5));
        }

        // ── Seed Offers ─────────────────────────────────────────────────────────────
        if (offerRepository.count() == 0) {
            offerRepository.save(new in.ds.ShopEase.model.Offer(null, "Grand Summer Sale", "Get massive discounts on all fashion items.", 50, "SUMMER50", java.time.LocalDate.now().plusMonths(3), "banner_sale.png", true));
            offerRepository.save(new in.ds.ShopEase.model.Offer(null, "Tech Bonanza", "Latest gadgets at unbeatable prices.", 20, "TECH20", java.time.LocalDate.now().plusMonths(1), "electronics.png", true));
        }
    }

    private void createProduct(String name, String brand, String desc, double price, double originalPrice, int stock, String image, String warranty, String specs, Category category) {
        in.ds.ShopEase.model.Product p = new in.ds.ShopEase.model.Product();
        p.setName(name);
        p.setBrand(brand);
        p.setDescription(desc);
        p.setPrice(price);
        p.setOriginalPrice(originalPrice);
        p.setStockQuantity(stock);
        p.setImageName(image);
        p.setWarranty(warranty);
        p.setSpecifications(specs);
        p.setCategory(category);
        productRepository.save(p);
    }
}
