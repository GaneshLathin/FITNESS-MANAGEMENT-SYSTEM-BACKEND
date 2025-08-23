package Fitness_Management_System.Fitness.dto;


import lombok.Data;
import java.util.List;

@Data
public class WorkoutPlanRequest {
    private String name;
    private List<ExerciseDetail> exercises;

    @Data
    public static class ExerciseDetail {
        private Long exerciseId;
        private int sets;
        private int reps;
        private int restTimeSeconds;
        private String dayOfWeek; // NEW

    }
}
