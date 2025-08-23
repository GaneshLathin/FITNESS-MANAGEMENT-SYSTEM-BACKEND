package Fitness_Management_System.Fitness.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkoutPlanClientDTO {
    private Long planId;
    private String planName;
    private List<ClientInfo> clients;

    @Data
    public static class ClientInfo {
        private Long clientId;
        private String clientName;
        private String clientEmail;
    }
}
