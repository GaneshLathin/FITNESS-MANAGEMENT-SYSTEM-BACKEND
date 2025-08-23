package Fitness_Management_System.Fitness.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_logs")
@Getter
@Setter
public class ProgressLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;           // The client who performed the exercise
    private Long planExerciseId;     // The specific exercise in a plan

    private int repsCompleted;       // How many reps user did
    private double weightLifted;     // Weight used (kg/lbs)

    private LocalDateTime logTime;   // When the log was recorded
}
