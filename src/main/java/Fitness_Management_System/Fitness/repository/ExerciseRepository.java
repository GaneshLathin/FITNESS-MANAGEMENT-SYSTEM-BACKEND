package Fitness_Management_System.Fitness.repository;

import Fitness_Management_System.Fitness.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

}
