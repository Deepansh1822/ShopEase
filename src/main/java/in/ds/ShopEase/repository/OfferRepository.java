package in.ds.ShopEase.repository;

import in.ds.ShopEase.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByActiveTrue();
}
