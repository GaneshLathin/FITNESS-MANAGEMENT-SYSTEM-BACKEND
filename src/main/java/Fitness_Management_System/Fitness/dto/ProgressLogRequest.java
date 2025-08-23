package Fitness_Management_System.Fitness.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressLogRequest {
    private Long planExerciseId;
    private int repsCompleted;
    private double weightLifted;
}
