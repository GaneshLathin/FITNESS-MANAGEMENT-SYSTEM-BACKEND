package Fitness_Management_System.Fitness.controller;

import Fitness_Management_System.Fitness.dto.ExerciseProgressDTO;
import Fitness_Management_System.Fitness.dto.ProgressLogRequest;
import Fitness_Management_System.Fitness.model.ProgressLog;
import Fitness_Management_System.Fitness.model.User;
import Fitness_Management_System.Fitness.repository.UserRepository;
import Fitness_Management_System.Fitness.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;
    // Log exercise progress
    @PostMapping("/progress/log")
    public ResponseEntity<Void> logExercise(@RequestBody ProgressLogRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        progressService.logExercise(client.getId(), request.getPlanExerciseId(), request.getRepsCompleted(), request.getWeightLifted());
        return ResponseEntity.ok().build();
    }

    // Get historical progress for a specific exercise
    @GetMapping("/progress/{planExerciseId}")
    public ResponseEntity<List<ProgressLog>> getExerciseHistory(@PathVariable Long planExerciseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        List<ProgressLog> history = progressService.getExerciseHistory(client.getId(), planExerciseId);
        return ResponseEntity.ok(history);
    }

    // Get all progress logs for client
    @GetMapping("/progress")
    public ResponseEntity<List<ProgressLog>> getAllProgress() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        List<ProgressLog> allLogs = progressService.getAllProgress(client.getId());
        return ResponseEntity.ok(allLogs);
    }

    // Get progress logs for chart (grouped by date) for one exercise
    @GetMapping("/exercise/{planExerciseId}/progress")
    public ResponseEntity<ExerciseProgressDTO> getExerciseProgress(
            @PathVariable Long planExerciseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail = auth.getName();

        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        ExerciseProgressDTO dto = progressService.getExerciseProgress(client.getId(), planExerciseId);
        return ResponseEntity.ok(dto);
    }

}
