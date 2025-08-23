package Fitness_Management_System.Fitness.repository;

import Fitness_Management_System.Fitness.model.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByTrainerId(Long trainerId);

}
