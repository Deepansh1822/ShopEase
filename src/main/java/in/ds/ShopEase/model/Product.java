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

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
