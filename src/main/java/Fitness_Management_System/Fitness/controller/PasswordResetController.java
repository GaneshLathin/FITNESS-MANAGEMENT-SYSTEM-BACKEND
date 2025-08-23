package Fitness_Management_System.Fitness.controller;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Fitness_Management_System.Fitness.service.PasswordResetService;
@RestController
@RequestMapping("/api/auth") // Consistent with your existing auth endpoints
@CrossOrigin(origins = "https://fitness-management-system-omega.vercel.app") // Ensure this matches your frontend URL
public class PasswordResetController {

    @Autowired
    public  PasswordResetService passwordResetService;


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String result = passwordResetService.initiatePasswordReset(request.getEmail());
        if (result.startsWith("User not found")) {
            return ResponseEntity.badRequest().body(result); // 400 for user not found
        }
        if (result.startsWith("Failed to send OTP") || result.startsWith("An unexpected error occurred")) {
            // Use 500 for internal server errors like email sending failures
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result); // 200 for OTP sent successfully
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String result = passwordResetService.resetPassword(request.getEmail(), request.getOtpCode(), request.getNewPassword());
        if (result.contains("Invalid") || result.contains("expired") || result.contains("used") || result.contains("New password cannot be the same")) {
            return ResponseEntity.badRequest().body(result); // 400 for validation errors
        }
        if (result.contains("User not found")) { // Though unlikely if flow is followed, a safety check
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result); // 200 for successful reset
    }

    // DTOs for requests (can be in separate files or nested as here)
    @Data
    static class ForgotPasswordRequest {
        private String email;
    }

    @Data
    static class ResetPasswordRequest {
        private String email;
        private String otpCode;
        private String newPassword;
    }
}