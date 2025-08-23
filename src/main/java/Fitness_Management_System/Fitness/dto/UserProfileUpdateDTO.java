package Fitness_Management_System.Fitness.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String name;
    private String email; // optional, only if you allow changing email
    // ⚠️ password excluded (must use OTP flow)
}
