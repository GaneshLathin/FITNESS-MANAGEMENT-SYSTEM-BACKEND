package Fitness_Management_System.Fitness.repository;

import Fitness_Management_System.Fitness.model.PlanExercise;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanExerciseRepository extends JpaRepository<PlanExercise, Long> {
    List<PlanExercise> findByWorkoutPlanId(Long workoutPlanId);
    void deleteByWorkoutPlanId(Long workoutPlanId);
    @Modifying
    @Transactional
    @Query("UPDATE PlanExercise p SET p.completed = true WHERE p.id = :planExerciseId")
    void markCompleted(Long planExerciseId);


}
