package Fitness_Management_System.Fitness.repository;

import Fitness_Management_System.Fitness.model.ClientWorkoutAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientWorkoutAssignmentRepository extends JpaRepository<ClientWorkoutAssignment, Long> {
    List<ClientWorkoutAssignment> findByClientId(Long clientId);
    void deleteByWorkoutPlanId(Long workoutPlanId);
    List<ClientWorkoutAssignment> findByWorkoutPlanId(Long workoutPlanId);
    // In ClientWorkoutAssignmentRepository


}
