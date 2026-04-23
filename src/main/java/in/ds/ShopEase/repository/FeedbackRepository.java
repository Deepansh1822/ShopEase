package in.ds.ShopEase.repository;

import in.ds.ShopEase.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop9ByOrderByCreatedAtDesc();
}
