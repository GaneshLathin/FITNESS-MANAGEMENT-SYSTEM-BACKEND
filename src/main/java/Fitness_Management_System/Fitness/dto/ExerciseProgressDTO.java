package Fitness_Management_System.Fitness.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ExerciseProgressDTO {
    private Long exerciseId;
    private String exerciseName;
    private List<DailyProgress> dailyProgress;

    @Data
    public static class DailyProgress {
        private LocalDate date;
        private int repsCompleted;
        private double weightLifted;
    }
}