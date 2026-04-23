package in.ds.ShopEase.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String subtitle; // e.g. "Verified Buyer"
    
    @Column(length = 500)
    private String message;
    
    private int rating; // 1 to 5

    private LocalDateTime createdAt = LocalDateTime.now();

    public Feedback() {}

    public Feedback(String name, String subtitle, String message, int rating) {
        this.name = name;
        this.subtitle = subtitle;
        this.message = message;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }
}
