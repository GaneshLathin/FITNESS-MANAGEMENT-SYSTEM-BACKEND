package Fitness_Management_System.Fitness.controller;

import Fitness_Management_System.Fitness.dto.UserProfileUpdateDTO;
import Fitness_Management_System.Fitness.dto.WorkoutPlanViewDTO;
import Fitness_Management_System.Fitness.model.Notification;
import Fitness_Management_System.Fitness.model.User;
import Fitness_Management_System.Fitness.service.ClientService;
import Fitness_Management_System.Fitness.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    public ClientService clientService;

    @Autowired
    public NotificationService notificationService;

    // FR2.3: A CLIENT can only view their own data
    @GetMapping("/my-profile")
    public ResponseEntity<User> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = authentication.getName(); // email from JWT

        User client = clientService.getMyProfile(clientEmail);
        if (client == null) {
            return ResponseEntity.notFound().build();
        }
        client.setPassword(null); // Mask sensitive data
        return ResponseEntity.ok(client);
    }
    @PutMapping("/my-profile")
    public ResponseEntity<User> updateMyProfile(@RequestBody UserProfileUpdateDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        User updated = clientService.updateMyProfile(clientEmail, dto);
        updated.setPassword(null); // never return password
        return ResponseEntity.ok(updated);
    }

    // FR2.3: A CLIENT can view their assigned trainer
    @GetMapping("/my-trainer")
    public ResponseEntity<User> getMyTrainer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = authentication.getName();

        User trainer = clientService.getMyTrainer(clientEmail);
        if (trainer == null) {
            return ResponseEntity.notFound().build();
        }
        trainer.setPassword(null);
        return ResponseEntity.ok(trainer);
    }

    // FR2.3: A CLIENT can view their assigned workout plan
    @GetMapping("/my-workout-plan")
    public ResponseEntity<WorkoutPlanViewDTO> getMyWorkoutPlan() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        // ✅ First get the client profile to fetch ID
        User client = clientService.getMyProfile(clientEmail);
        if (client == null) {
            return ResponseEntity.notFound().build();
        }

        WorkoutPlanViewDTO plan = clientService.getMyWorkoutPlan(client.getId());
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User client = clientService.getMyProfile(email);
        return ResponseEntity.ok(notificationService.getUserNotifications(client.getId()));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }


    // ==============================
// ClientController.java
// ==============================

    @PostMapping("/chat/send")
    public ResponseEntity<String> sendMessageToTrainer(@RequestParam String message) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        clientService.sendMessageToTrainer(clientEmail, message);
        return ResponseEntity.ok("Message sent to trainer.");
    }

    @PostMapping("/request/new-plan")
    public ResponseEntity<String> requestNewPlan(@RequestParam(required = false) String notes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        clientService.requestNewPlan(clientEmail, notes);
        return ResponseEntity.ok("New workout plan request sent.");
    }

    @PostMapping("/request/difficulty")
    public ResponseEntity<String> requestDifficultyChange(@RequestParam String difficultyLevel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        clientService.requestDifficultyChange(clientEmail, difficultyLevel);
        return ResponseEntity.ok("Difficulty adjustment request sent.");
    }


}
