package Fitness_Management_System.Fitness.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkoutPlanDetailDTO {
    private Long planId;              // ✅ add this
    private String name;
    private List<ExerciseDetail> exercises;

    @Data
    public static class ExerciseDetail {
        private Long planExerciseId;
        private String name;
        private String description;
        private String targetMuscles;
        private int sets;
        private int reps;
        private int restTimeSeconds;
        private String dayOfWeek;
    }
}
