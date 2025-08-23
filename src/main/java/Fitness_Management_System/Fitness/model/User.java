package Fitness_Management_System.Fitness.model;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import jakarta.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role; // ADMIN / TRAINER / CLIENT

    // New field to link a client to their assigned trainer
    // This will be null for ADMINs and TRAINERs, and for CLIENTs not yet assigned
    private Long assignedTrainerId;
}
