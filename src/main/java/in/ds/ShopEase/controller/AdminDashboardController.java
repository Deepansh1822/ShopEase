package in.ds.ShopEase.controller;

import in.ds.ShopEase.model.Category;
import in.ds.ShopEase.model.Product;
import in.ds.ShopEase.repository.CategoryRepository;
import in.ds.ShopEase.repository.OrderRepository;
import in.ds.ShopEase.repository.ProductRepository;
import in.ds.ShopEase.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private in.ds.ShopEase.repository.OfferRepository offerRepository;

    public static String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/img";

    @GetMapping({"", "/"})
    public String dashboardRoot() {
        return "redirect:/admin/products";
    }

    @GetMapping("/products")
    public String productsPage(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("totalCategories", categoryRepository.count());
        return "adminProducts";
    }

    @GetMapping("/products/add")
    public String addProductPage(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        return "adminProductForm";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                             @RequestParam("imgFile1") MultipartFile imgFile1,
                             @RequestParam("imgFile2") MultipartFile imgFile2,
                             @RequestParam("imgFile3") MultipartFile imgFile3,
                             @RequestParam("imgFile4") MultipartFile imgFile4) {
        
        handleImageUpload(product, imgFile1, 1);
        handleImageUpload(product, imgFile2, 2);
        handleImageUpload(product, imgFile3, 3);
        handleImageUpload(product, imgFile4, 4);

        if (product.getImageName() == null || product.getImageName().isEmpty()) {
            product.setImageName("placeholder.png");
        }
        productRepository.save(product);
        return "redirect:/admin/products";
    }

    private void handleImageUpload(Product product, MultipartFile file, int index) {
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            try {
                Path path = Paths.get(uploadDir, fileName);
                Files.write(path, file.getBytes());
                
                switch (index) {
                    case 1 -> product.setImageName(fileName);
                    case 2 -> product.setImageName2(fileName);
                    case 3 -> product.setImageName3(fileName);
                    case 4 -> product.setImageName4(fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/products/edit/{id}")
    public String editProductPage(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id).get());
        model.addAttribute("categories", productService.getAllCategories());
        return "adminProductForm";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String ordersHistoryPage(Model model) {
        java.util.List<in.ds.ShopEase.model.Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("paidCount", orders.stream().filter(o -> "paid".equals(o.getStatus())).count());
        model.addAttribute("pendingCount", orders.stream().filter(o -> "created".equals(o.getStatus())).count());
        return "adminOrders";
    }

    /** AJAX endpoint — saves a new category from the modal and returns JSON */
    @PostMapping("/categories/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveCategory(@RequestParam("name") String name,
                                                           @RequestParam(value = "description", required = false) String description,
                                                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            try {
                Path path = Paths.get(uploadDir, fileName);
                Files.write(path, imageFile.getBytes());
                category.setImageName(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            category.setImageName("category_placeholder.png");
        }

        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "name", saved.getName(),
                "imageName", saved.getImageName()
        ));
    }

    // ── Offers Management ───────────────────────────────────────────────────

    @GetMapping("/offers")
    public String adminOffersPage(Model model) {
        model.addAttribute("offers", offerRepository.findAll());
        return "adminOffers";
    }

    @GetMapping("/offers/add")
    public String addOfferPage(Model model) {
        model.addAttribute("offer", new in.ds.ShopEase.model.Offer());
        return "adminOfferForm";
    }

    @PostMapping("/offers/save")
    public String saveOffer(@ModelAttribute in.ds.ShopEase.model.Offer offer,
                           @RequestParam("imageFile") MultipartFile imageFile) {
        if (!imageFile.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            try {
                Path path = Paths.get(uploadDir, fileName);
                Files.write(path, imageFile.getBytes());
                offer.setImageName(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (offer.getImageName() == null || offer.getImageName().isEmpty()) {
            offer.setImageName("offer_placeholder.png");
        }
        
        offerRepository.save(offer);
        return "redirect:/admin/offers";
    }

    @GetMapping("/offers/edit/{id}")
    public String editOfferPage(@PathVariable Long id, Model model) {
        model.addAttribute("offer", offerRepository.findById(id).orElseThrow());
        return "adminOfferForm";
    }

    @GetMapping("/offers/delete/{id}")
    public String deleteOffer(@PathVariable Long id) {
        offerRepository.deleteById(id);
        return "redirect:/admin/offers";
    }

    @GetMapping("/offers/toggle/{id}")
    public String toggleOffer(@PathVariable Long id) {
        in.ds.ShopEase.model.Offer offer = offerRepository.findById(id).orElseThrow();
        offer.setActive(!offer.isActive());
        offerRepository.save(offer);
        return "redirect:/admin/offers";
    }
}

