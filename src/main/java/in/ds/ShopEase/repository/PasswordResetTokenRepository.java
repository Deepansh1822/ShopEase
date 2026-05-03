package in.ds.ShopEase.repository;

import in.ds.ShopEase.model.PasswordResetToken;
import in.ds.ShopEase.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
    @org.springframework.transaction.annotation.Transactional
    void deleteByUser(User user);
    
    @org.springframework.transaction.annotation.Transactional
    void deleteByToken(String token);
}
