package in.ds.ShopEase.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Product product;

    private LocalDateTime addedAt;

    public Wishlist(User user, Product product) {
        this.user = user;
        this.product = product;
        this.addedAt = LocalDateTime.now();
    }
}
