// ==============================
// ClientService.java
// ==============================
package Fitness_Management_System.Fitness.service;

import Fitness_Management_System.Fitness.dto.UserProfileUpdateDTO;
import Fitness_Management_System.Fitness.dto.WorkoutPlanViewDTO;
import Fitness_Management_System.Fitness.model.*;
import Fitness_Management_System.Fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final ClientWorkoutAssignmentRepository clientWorkoutAssignmentRepository;
    private final ProgressLogRepository progressLogRepository;
    private final NotificationService notificationService;

    /**
     * Fetch client profile by email.
     */
    public User getMyProfile(String clientEmail) {
        return userRepository.findByEmail(clientEmail)
                .filter(u -> "CLIENT".equalsIgnoreCase(u.getRole()))
                .orElseThrow(() -> new RuntimeException("Client not found with email: " + clientEmail));
    }

    public User updateMyProfile(String email, UserProfileUpdateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());

        // 🚫 password update is intentionally excluded

        return userRepository.save(user);
    }

    /**
     * Fetch trainer assigned to this client.
     */
    public User getMyTrainer(String clientEmail) {
        User client = getMyProfile(clientEmail);

        if (client.getAssignedTrainerId() == null) {
            throw new RuntimeException("No trainer assigned for this client");
        }

        return userRepository.findById(client.getAssignedTrainerId())
                .filter(t -> "TRAINER".equalsIgnoreCase(t.getRole()))
                .orElseThrow(() -> new RuntimeException("Assigned trainer not found"));
    }

    /**
     * Fetch client's assigned workout plan with exercises grouped by day.
     */
    public WorkoutPlanViewDTO getMyWorkoutPlan(Long clientId) {
        List<ClientWorkoutAssignment> assignments = clientWorkoutAssignmentRepository.findByClientId(clientId);

        if (assignments.isEmpty()) {
            throw new RuntimeException("No workout plan assigned for this client");
        }

        // ✅ Business rule: choose latest assigned plan (highest ID = most recent)
        assignments.sort(Comparator.comparingLong(ClientWorkoutAssignment::getId));
        ClientWorkoutAssignment assignment = assignments.get(assignments.size() - 1);

        WorkoutPlan plan = workoutPlanRepository.findById(assignment.getWorkoutPlanId())
                .orElseThrow(() -> new RuntimeException("Workout plan not found"));

        WorkoutPlanViewDTO dto = new WorkoutPlanViewDTO();
        dto.setPlanName(plan.getName());

        List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanId(plan.getId());

        // ✅ Group exercises by day of week
        Map<String, List<PlanExercise>> groupedByDay = planExercises.stream()
                .collect(Collectors.groupingBy(PlanExercise::getDayOfWeek));

        List<WorkoutPlanViewDTO.DayPlan> days = new ArrayList<>();

        for (Map.Entry<String, List<PlanExercise>> entry : groupedByDay.entrySet()) {
            WorkoutPlanViewDTO.DayPlan dayPlan = new WorkoutPlanViewDTO.DayPlan();
            dayPlan.setDayOfWeek(entry.getKey());

            List<WorkoutPlanViewDTO.ExerciseView> exerciseViews = new ArrayList<>();

            for (PlanExercise pe : entry.getValue()) {
                Exercise ex = exerciseRepository.findById(pe.getExerciseId())
                        .orElseThrow(() -> new RuntimeException("Exercise not found with ID: " + pe.getExerciseId()));

                // ✅ Fetch today's completed reps
                int totalRepsCompleted = Optional
                        .ofNullable(progressLogRepository.getTotalRepsCompletedToday(clientId, pe.getId()))
                        .orElse(0);

                WorkoutPlanViewDTO.ExerciseView ev = new WorkoutPlanViewDTO.ExerciseView();
                ev.setId(pe.getId()); // ✅ Set PlanExercise ID
                ev.setName(ex.getName());
                ev.setDescription(ex.getDescription());
                ev.setTargetMuscles(ex.getTargetMuscles());
                ev.setSets(pe.getSets());
                ev.setReps(pe.getReps());
                ev.setRestTimeSeconds(pe.getRestTimeSeconds());

                // ✅ Mark completed if reps done >= sets × reps
                boolean isCompleted = totalRepsCompleted >= (pe.getSets() * pe.getReps());
                ev.setCompleted(isCompleted);

                exerciseViews.add(ev);
            }

            dayPlan.setExercises(exerciseViews);
            days.add(dayPlan);
        }

        dto.setDays(days);
        return dto;
    }


    // ==============================
// ClientService.java
// ==============================


    public void sendMessageToTrainer(String clientEmail, String message) {
        User client = getMyProfile(clientEmail);
        User trainer = getMyTrainer(clientEmail);

        notificationService.sendNotification(
                trainer.getId(),
                "Message from " + client.getName() + ": " + message
        );
    }

    public void requestNewPlan(String clientEmail, String notes) {
        User client = getMyProfile(clientEmail);
        User trainer = getMyTrainer(clientEmail);

        String msg = "New workout plan requested by " + client.getName();
        if (notes != null) {
            msg += " (Notes: " + notes + ")";
        }

        notificationService.sendNotification(trainer.getId(), msg);
    }

    public void requestDifficultyChange(String clientEmail, String difficultyLevel) {
        User client = getMyProfile(clientEmail);
        User trainer = getMyTrainer(clientEmail);

        notificationService.sendNotification(
                trainer.getId(),
                "Difficulty change requested by " + client.getName() + " → " + difficultyLevel
        );
    }



}
