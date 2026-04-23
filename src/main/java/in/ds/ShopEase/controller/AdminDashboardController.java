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

import java.util.Map;

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
    public String saveProduct(@ModelAttribute Product product) {
        if (product.getImageName() == null || product.getImageName().isEmpty()) {
            product.setImageName("placeholder.png");
        }
        productRepository.save(product);
        return "redirect:/admin/products";
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
    public ResponseEntity<Map<String, Object>> saveCategory(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "name", saved.getName()
        ));
    }
}

