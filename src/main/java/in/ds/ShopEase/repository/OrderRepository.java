package in.ds.ShopEase.repository;

import in.ds.ShopEase.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderId(String orderId);
    java.util.List<Order> findByUserOrderByCreatedAtDesc(in.ds.ShopEase.model.User user);
}
