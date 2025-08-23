package Fitness_Management_System.Fitness.controller;

import Fitness_Management_System.Fitness.dto.*;
import Fitness_Management_System.Fitness.model.*;
import Fitness_Management_System.Fitness.repository.ExerciseRepository;
import Fitness_Management_System.Fitness.repository.UserRepository;
import Fitness_Management_System.Fitness.service.NotificationService;
import Fitness_Management_System.Fitness.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping("/exercises")
    public List<Exercise> listExercises() {
        return exerciseRepository.findAll();
    }

    @GetMapping("/my-clients")
    public ResponseEntity<List<User>> getMyClients() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = authentication.getName();
        List<User> clients = trainerService.getMyClients(trainerEmail);
        if (clients.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(clients);
    }

    @PostMapping("/workout-plans")
    public ResponseEntity<WorkoutPlan> createWorkoutPlan(@RequestBody WorkoutPlanRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = auth.getName();
        WorkoutPlan plan = trainerService.createWorkoutPlan(trainerEmail, request);
        return ResponseEntity.ok(plan);
    }

    @PutMapping("/workout-plans/{planId}")
    public ResponseEntity<WorkoutPlan> updateWorkoutPlan(@PathVariable Long planId, @RequestBody WorkoutPlanRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = auth.getName();
        WorkoutPlan updatedPlan = trainerService.updateWorkoutPlan(trainerEmail, planId, request);
        return ResponseEntity.ok(updatedPlan);
    }
    @PutMapping("/plan-exercise/{planExerciseId}")
    public ResponseEntity<Void> updatePlanExerciseDetails(
            @PathVariable Long planExerciseId,
            @RequestBody UpdatePlanExerciseDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = auth.getName();
        trainerService.updatePlanExercise(trainerEmail, planExerciseId, dto);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/workout-plans")
    public ResponseEntity<List<WorkoutPlan>> getAllWorkoutPlans() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = auth.getName();
        List<WorkoutPlan> plans = trainerService.getWorkoutPlansByTrainer(trainerEmail);
        if (plans.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(plans);
    }
//    @GetMapping("/workout-plans/details")
//    public ResponseEntity<List<WorkoutPlanDetailDTO>> getAllWorkoutPlanDetails() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String trainerEmail = auth.getName();
//        List<WorkoutPlanDetailDTO> details = trainerService.getAllWorkoutPlanDetails(trainerEmail);
//        if (details.isEmpty()) return ResponseEntity.noContent().build();
//        return ResponseEntity.ok(details);
//    }
// ✅ Get particular workout plan details by ID
@GetMapping("/workout-plans/{planId}")
public ResponseEntity<WorkoutPlanDetailDTO> getWorkoutPlanDetailById(@PathVariable Long planId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String trainerEmail = auth.getName();
    WorkoutPlanDetailDTO detail = trainerService.getWorkoutPlanDetailById(planId, trainerEmail);
    return ResponseEntity.ok(detail);
}


    @PostMapping("/assign-plan")
    public ResponseEntity<Void> assignPlan(@RequestParam Long clientId, @RequestParam Long planId) {
        trainerService.assignWorkoutPlan(clientId, planId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/workout-plans/clients")
    public ResponseEntity<List<WorkoutPlanClientDTO>> getAllWorkoutPlansWithClients() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = auth.getName();
        List<WorkoutPlanClientDTO> dtos = trainerService.getAllWorkoutPlansWithClients(trainerEmail);

        if (dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }


    @DeleteMapping("/workout-plans/{planId}")
    public ResponseEntity<Void> deleteWorkoutPlan(@PathVariable Long planId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = auth.getName();
        trainerService.deleteWorkoutPlan(trainerEmail, planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/client/{clientId}/progress-detail")
    public ResponseEntity<ClientProgressDetailDTO> getClientProgressDetail(@PathVariable Long clientId) {
        return ResponseEntity.ok(trainerService.getClientProgressDetail(clientId));
    }

    /**
     * FR7.2: Get exercise progress report of a client
     */
    @GetMapping("/client/{clientId}/exercise/{planExerciseId}/report")
    public ResponseEntity<ExerciseProgressDTO> getExerciseProgress(
            @PathVariable Long clientId,
            @PathVariable Long planExerciseId) {
        return ResponseEntity.ok(trainerService.getExerciseProgressReport(clientId, planExerciseId));
    }


    @PostMapping("/feedback/{clientId}")
    public ResponseEntity<Feedback> giveFeedback(
            @PathVariable Long clientId,
            @RequestBody FeedbackRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String trainerEmail = auth.getName();
        return ResponseEntity.ok(trainerService.giveFeedback(trainerEmail, clientId, request.getComments(), request.getRating()));
    }

    @GetMapping("/notifications")
    public List<Notification> getTrainerNotifications(Authentication auth) {
        String email = auth.getName();
        User trainer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        return notificationService.getUserNotifications(trainer.getId());
    }

}
