package Fitness_Management_System.Fitness.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Fitness_Management_System.Fitness.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // New: Find users by role
    List<User> findByRole(String role);

    // New: Find clients by their assigned trainer's ID (FR2.2)
    List<User> findByAssignedTrainerId(Long trainerId);
}
