package in.ds.ShopEase.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "product_reviews")
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String reviewerName;

    @Column(length = 1000)
    private String reviewText;

    private int rating; // 1-5

    @ElementCollection
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_name")
    private java.util.List<String> imageNames = new java.util.ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    public ProductReview() {}

    public ProductReview(Product product, String reviewerName, String reviewText, int rating, java.util.List<String> imageNames) {
        this.product = product;
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.rating = rating;
        this.imageNames = imageNames;
        this.createdAt = LocalDateTime.now();
    }
}
