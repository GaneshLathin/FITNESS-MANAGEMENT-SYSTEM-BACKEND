    package Fitness_Management_System.Fitness.model;

    import jakarta.persistence.*;
    import lombok.Data;

    import java.time.LocalDateTime;

    @Entity
    @Data
    public class Notification {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private Long userId;              // client receiving the notification
        private String message;           // notification text
        @Column(name = "is_read") // Use a non-reserved name
        private boolean isRead=false;
        private LocalDateTime createdAt = LocalDateTime.now();
    }
