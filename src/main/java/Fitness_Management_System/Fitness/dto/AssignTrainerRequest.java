package Fitness_Management_System.Fitness.dto;

import lombok.Data;

@Data
public class AssignTrainerRequest {
    private Long clientId;
    private Long trainerId;
}
