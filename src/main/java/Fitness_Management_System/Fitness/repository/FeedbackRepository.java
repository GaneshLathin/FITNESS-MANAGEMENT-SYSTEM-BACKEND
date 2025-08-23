package Fitness_Management_System.Fitness.repository;

import Fitness_Management_System.Fitness.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByClientId(Long clientId);
}
