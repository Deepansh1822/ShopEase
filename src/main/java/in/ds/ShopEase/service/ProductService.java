package in.ds.ShopEase.service;

import in.ds.ShopEase.model.Category;
import in.ds.ShopEase.model.Product;
import in.ds.ShopEase.repository.CategoryRepository;
import in.ds.ShopEase.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategoryId(Long id) {
        return productRepository.findAllByCategoryId(id);
    }
}
