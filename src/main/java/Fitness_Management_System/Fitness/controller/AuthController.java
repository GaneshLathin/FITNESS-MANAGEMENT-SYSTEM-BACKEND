package Fitness_Management_System.Fitness.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity; // Import ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import Fitness_Management_System.Fitness.dto.LoginRequest;
import Fitness_Management_System.Fitness.dto.RegisterRequest;
import Fitness_Management_System.Fitness.model.User;
import Fitness_Management_System.Fitness.repository.UserRepository;
import Fitness_Management_System.Fitness.security.JwtUtil;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public  PasswordEncoder passwordEncoder;
    @Autowired
    public JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ResponseEntity<>("Email already registered!", HttpStatus.CONFLICT);
        }

        // Always assign CLIENT role for self-registration
        String role = "CLIENT";

        // If a logged-in ADMIN is creating a user, allow custom roles
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String creatorRole = jwtUtil.extractRole(token);
            if ("ADMIN".equals(creatorRole)) {
                // Here ADMIN can decide role — hardcoded for example
                role = "TRAINER"; // or "ADMIN" if needed
            }
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }


   @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
    if (optionalUser.isEmpty() || 
        !passwordEncoder.matches(request.getPassword(), optionalUser.get().getPassword())) {
        return new ResponseEntity<>("Invalid email or password!", HttpStatus.UNAUTHORIZED);
    }

    User user = optionalUser.get();
    String token = jwtUtil.generateToken(user.getEmail(), user.getRole(),user.getId());

    return ResponseEntity.ok(
        Map.of("token", token, "role", user.getRole())
    );
}

}