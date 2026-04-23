package in.ds.ShopEase.repository;

import in.ds.ShopEase.model.Product;
import in.ds.ShopEase.model.User;
import in.ds.ShopEase.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserOrderByAddedAtDesc(User user);
    Optional<Wishlist> findByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
}
