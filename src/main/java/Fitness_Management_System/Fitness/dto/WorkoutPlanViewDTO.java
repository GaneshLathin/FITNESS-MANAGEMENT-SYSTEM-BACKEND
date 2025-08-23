package Fitness_Management_System.Fitness.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkoutPlanViewDTO {
    private String planName;
    private List<DayPlan> days;

    @Data
    public static class DayPlan {
        private String dayOfWeek;
        private List<ExerciseView> exercises;
    }

    @Data
    public static class ExerciseView {
        private Long id; // ✅ PlanExercise ID (used for progress tracking)
        private String name;
        private String description;
        private String targetMuscles;
        private int sets;
        private int reps;
        private int restTimeSeconds;
        private boolean completed; // ✅ FR5.3 visual indicator
    }
}
