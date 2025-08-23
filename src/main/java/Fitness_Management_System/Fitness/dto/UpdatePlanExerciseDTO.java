// dto/UpdatePlanExerciseDTO.java
package Fitness_Management_System.Fitness.dto;

import lombok.Data;

@Data
public class UpdatePlanExerciseDTO {
    private int sets;
    private int reps;
    private int restTimeSeconds;
    private String dayOfWeek;
}
