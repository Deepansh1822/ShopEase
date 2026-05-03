package in.ds.ShopEase.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;
    private String description;
    private double price;
    private String imageName;
    private String imageName2;
    private String imageName3;
    private String imageName4;
    private String warranty;
    private String specifications;
    
    // Power Fields
    private double originalPrice;
    private int stockQuantity;
    private String productCode;
    
    // Category Specific (Optional)
    private String material;
    private String size;
    private String weight;
    private String capacity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @PrePersist
    public void generateProductCode() {
        if (this.productCode == null || this.productCode.isEmpty()) {
            this.productCode = "PROD-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
