package Fitness_Management_System.Fitness.model;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity// This maps your Otp class to an 'otps' collection in MongoDB
public class Otp {

    @Id // MongoDB typically uses String for _id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String otpCode;

    // We can use an expiry index in MongoDB for automatic cleanup, but we'll manage manually for now.
    // @Indexed(expireAfterSeconds = 300) // This is for automatic deletion after 300 seconds (5 minutes)
    private LocalDateTime expiryTime;

    private boolean used; // To ensure OTPs are used only once
}