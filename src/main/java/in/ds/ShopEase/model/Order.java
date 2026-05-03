package in.ds.ShopEase.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String orderId; // Razorpay order id
    private String paymentId;
    private double amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;

    @ManyToOne
    private User user;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingCity;
    private String shippingPin;
    
    private String returnReason;
    private String returnStatus; // e.g., "pending", "approved", "rejected"

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    public boolean isReturnEligible() {
        if (deliveredAt == null || !"delivered".equals(status)) return false;
        long days = java.time.Duration.between(deliveredAt, LocalDateTime.now()).toDays();
        int limit = (user != null && user.isMember()) ? 14 : 7;
        return days <= limit;
    }
}
