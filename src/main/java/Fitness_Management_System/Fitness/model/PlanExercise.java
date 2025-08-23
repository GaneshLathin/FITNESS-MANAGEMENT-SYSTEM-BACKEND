package Fitness_Management_System.Fitness.model;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Table(name = "plan_exercises")
@Data
public class PlanExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long workoutPlanId;
    private Long exerciseId;
    private int sets;
    private int reps;
    private int restTimeSeconds;
    private String dayOfWeek; // "Monday", "Tuesday", etc.
    private boolean completed = false; // ✅ new field


}
