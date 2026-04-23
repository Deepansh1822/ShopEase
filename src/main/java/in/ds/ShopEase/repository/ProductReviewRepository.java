package in.ds.ShopEase.repository;

import in.ds.ShopEase.model.Product;
import in.ds.ShopEase.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductOrderByCreatedAtDesc(Product product);
}
