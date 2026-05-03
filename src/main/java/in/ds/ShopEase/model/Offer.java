package in.ds.ShopEase.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private int discountPercentage;
    private String promoCode;
    private LocalDate expiryDate;
    private String imageName;
    private boolean active = true;
}
