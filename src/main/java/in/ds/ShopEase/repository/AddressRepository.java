package in.ds.ShopEase.repository;

import in.ds.ShopEase.model.Address;
import in.ds.ShopEase.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}
