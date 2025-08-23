package Fitness_Management_System.Fitness.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long trainerId;
    private Long clientId;

    @Column(length = 1000)
    private String comments;

    private int rating; // 1–5 scale
    private LocalDateTime givenAt;
}
