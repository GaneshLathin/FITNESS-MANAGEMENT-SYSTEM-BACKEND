package Fitness_Management_System.Fitness.service;
import jakarta.mail.MessagingException; // Import for JavaMail exception
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Still useful for MongoDB if you define transactions

import Fitness_Management_System.Fitness.model.Otp;
import Fitness_Management_System.Fitness.model.User;
import Fitness_Management_System.Fitness.repository.OtpRepository;
import Fitness_Management_System.Fitness.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository, OtpRepository otpRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional // @Transactional works with MongoDB as well if you configure transaction management
    public String initiatePasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return "User not found with this email. Please register.";
        }

        // Invalidate any existing active OTPs for this email to prevent multiple valid OTPs
        // This query (findTopByEmailOrderByExpiryTimeDesc) is specific to Spring Data JPA,
        // For MongoDB, it will work if you ensure proper indexing and potentially custom queries
        // if you encounter issues. Spring Data MongoDB should handle findTopBy... for latest.
        otpRepository.findTopByEmailOrderByExpiryTimeDesc(email).ifPresent(otp -> {
            if (otp.getExpiryTime().isAfter(LocalDateTime.now()) && !otp.isUsed()) {
                otp.setUsed(true); // Mark as used/invalidated
                otpRepository.save(otp);
            }
        });

        String otpCode = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5); // OTP valid for 5 minutes

        Otp otp = Otp.builder()
                .email(email)
                .otpCode(otpCode)
                .expiryTime(expiryTime)
                .used(false)
                .build();
        otpRepository.save(otp);

        try {
            emailService.sendOtpEmail(email, otpCode);
            return "OTP sent to your email. Please check your inbox.";
        } catch (MessagingException e) { // Changed from ApiException to MessagingException
            // Log the exception details for debugging
            System.err.println("Error sending OTP email: " + e.getMessage());
            e.printStackTrace(); // Good for development, remove or use a logger in production
            return "Failed to send OTP. Please try again later.";
        } catch (Exception e) { // Catch any other unexpected exceptions
            System.err.println("An unexpected error occurred during OTP sending: " + e.getMessage());
            e.printStackTrace();
            return "An unexpected error occurred. Please try again.";
        }
    }

    @Transactional
    public String resetPassword(String email, String otpCode, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            // This case should ideally be caught earlier or prevented by UI flow,
            // but a defensive check is good.
            return "User not found.";
        }
        User user = userOptional.get();

        Optional<Otp> otpOptional = otpRepository.findByEmailAndOtpCodeAndUsedFalse(email, otpCode);

        if (otpOptional.isEmpty()) {
            return "Invalid or expired OTP.";
        }

        Otp otp = otpOptional.get();

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            otp.setUsed(true); // Mark expired OTP as used
            otpRepository.save(otp);
            return "OTP has expired. Please request a new one.";
        }

        if (otp.isUsed()) {
            // This is a safety check; findByEmailAndOtpCodeAndUsedFalse already handles it,
            // but it's good to have explicit logic if the state somehow changes before save.
            return "OTP has already been used.";
        }

        // Check if new password is the same as old password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return "New password cannot be the same as the old password.";
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark OTP as used
        otp.setUsed(true);
        otpRepository.save(otp);

        return "Password reset successfully!";
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates a 6-digit OTP
        return String.valueOf(otp);
    }
}