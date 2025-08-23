package Fitness_Management_System.Fitness.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ClientProgressDetailDTO {

    private Long clientId;
    private String clientName;

    // Progress over time (for line charts / time-series)
    private List<DailyProgress> dailyProgress;

    // Progress per exercise (for bar/pie charts)
    private List<ExerciseProgress> exerciseProgress;

    @Data
    public static class DailyProgress {
        private LocalDate date;
        private int totalReps;
        private double totalWeightLifted;
    }

    @Data
    public static class ExerciseProgress {
        private String exerciseName;
        private int totalReps;
        private double totalWeightLifted;
    }
}
