package Fitness_Management_System.Fitness.service;

import Fitness_Management_System.Fitness.dto.*;
import Fitness_Management_System.Fitness.model.*;
import Fitness_Management_System.Fitness.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final ClientWorkoutAssignmentRepository clientWorkoutAssignmentRepository;
    private final ProgressLogRepository progressLogRepository;
     private final NotificationService notificationService;
     private final FeedbackRepository feedbackRepository;

    // Get clients assigned to trainer
    public List<User> getMyClients(String trainerEmail) {
        Optional<User> trainerOpt = userRepository.findByEmail(trainerEmail);
        if (trainerOpt.isEmpty() || !"TRAINER".equalsIgnoreCase(trainerOpt.get().getRole())) {
            throw new SecurityException("Authenticated user is not a valid trainer.");
        }
        Long trainerId = trainerOpt.get().getId();
        return userRepository.findByAssignedTrainerId(trainerId);
    }

    // Create workout plan
    @Transactional
    public WorkoutPlan createWorkoutPlan(String trainerEmail, WorkoutPlanRequest request) {
        User trainer = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (!"TRAINER".equalsIgnoreCase(trainer.getRole())) {
            throw new SecurityException("Not authorized to create workout plans");
        }

        WorkoutPlan plan = new WorkoutPlan();
        plan.setTrainerId(trainer.getId());
        plan.setName(request.getName());
        workoutPlanRepository.save(plan);

        saveExercises(plan, request.getExercises());

        return plan;
    }

    // Update workout plan
    @Transactional
    public WorkoutPlan updateWorkoutPlan(String trainerEmail, Long planId, WorkoutPlanRequest request) {
        User trainer = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Workout plan not found"));

        if (!plan.getTrainerId().equals(trainer.getId())) {
            throw new SecurityException("Not authorized to update this workout plan");
        }

        // Update plan name
        plan.setName(request.getName());
        workoutPlanRepository.save(plan);

        // Remove existing exercises
        planExerciseRepository.deleteByWorkoutPlanId(planId);

        // Add new exercises
        saveExercises(plan, request.getExercises());

        return plan;
    }

    // Helper method to save exercises
    private void saveExercises(WorkoutPlan plan, List<WorkoutPlanRequest.ExerciseDetail> exercises) {
        for (WorkoutPlanRequest.ExerciseDetail ex : exercises) {
            Exercise exercise = exerciseRepository.findById(ex.getExerciseId())
                    .orElseThrow(() -> new RuntimeException("Exercise not found: " + ex.getExerciseId()));

            PlanExercise pe = new PlanExercise();
            pe.setWorkoutPlanId(plan.getId());
            pe.setExerciseId(exercise.getId());
            pe.setSets(ex.getSets());
            pe.setReps(ex.getReps());
            pe.setRestTimeSeconds(ex.getRestTimeSeconds());
            pe.setDayOfWeek(ex.getDayOfWeek());
            planExerciseRepository.save(pe);
        }
    }

    // Assign a plan to a client
    @Transactional
    public void assignWorkoutPlan(Long clientId, Long planId) {
        if (!workoutPlanRepository.existsById(planId)) {
            throw new RuntimeException("Plan not found");
        }
        ClientWorkoutAssignment assignment = new ClientWorkoutAssignment();
        assignment.setClientId(clientId);
        assignment.setWorkoutPlanId(planId);
        clientWorkoutAssignmentRepository.save(assignment);

        // ✅ Send notification
        notificationService.sendNotification(clientId, "A new workout plan has been assigned to you!");
    }


    // Delete workout plan
    @Transactional
    public void deleteWorkoutPlan(String trainerEmail, Long planId) {
        User trainer = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Workout plan not found"));

        if (!plan.getTrainerId().equals(trainer.getId())) {
            throw new SecurityException("Not authorized to delete this workout plan");
        }

        planExerciseRepository.deleteByWorkoutPlanId(planId);
        clientWorkoutAssignmentRepository.deleteByWorkoutPlanId(planId);
        workoutPlanRepository.deleteById(planId);
    }

    // Get all workout plans created by trainer
    public List<WorkoutPlan> getWorkoutPlansByTrainer(String trainerEmail) {
        User trainer = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        return workoutPlanRepository.findByTrainerId(trainer.getId());
    }



    public ClientProgressDetailDTO getClientProgressDetail(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        ClientProgressDetailDTO dto = new ClientProgressDetailDTO();
        dto.setClientId(clientId);
        dto.setClientName(client.getName());

        // Daily Progress
        List<Object[]> dailyRows = progressLogRepository.getDailyProgress(clientId);
        List<ClientProgressDetailDTO.DailyProgress> daily = dailyRows.stream().map(r -> {
            ClientProgressDetailDTO.DailyProgress dp = new ClientProgressDetailDTO.DailyProgress();
            dp.setDate(((java.sql.Date) r[0]).toLocalDate());
            dp.setTotalReps(((Long) r[1]).intValue());
            dp.setTotalWeightLifted((Double) r[2]);
            return dp;
        }).toList();
        dto.setDailyProgress(daily);

        // Exercise Progress
        List<Object[]> exerciseRows = progressLogRepository.getExerciseProgress(clientId);
        List<ClientProgressDetailDTO.ExerciseProgress> exercises = exerciseRows.stream().map(r -> {
            ClientProgressDetailDTO.ExerciseProgress ep = new ClientProgressDetailDTO.ExerciseProgress();
            ep.setExerciseName((String) r[0]);
            ep.setTotalReps(((Long) r[1]).intValue());
            ep.setTotalWeightLifted((Double) r[2]);
            return ep;
        }).toList();
        dto.setExerciseProgress(exercises);

        return dto;
    }

    /**
     * FR7.2: Get progress report for specific exercise of a client
     */
    public ExerciseProgressDTO getExerciseProgressReport(Long clientId, Long planExerciseId) {
        PlanExercise pe = planExerciseRepository.findById(planExerciseId)
                .orElseThrow(() -> new RuntimeException("Plan exercise not found"));

        Exercise exercise = exerciseRepository.findById(pe.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        List<Object[]> rawData = progressLogRepository.getDailyProgress(clientId);

        ExerciseProgressDTO dto = new ExerciseProgressDTO();
        dto.setExerciseId(exercise.getId());
        dto.setExerciseName(exercise.getName());

        List<ExerciseProgressDTO.DailyProgress> dailyList = rawData.stream().map(row -> {
            ExerciseProgressDTO.DailyProgress dp = new ExerciseProgressDTO.DailyProgress();
            dp.setDate(((java.sql.Date) row[0]).toLocalDate());
            dp.setRepsCompleted(((Number) row[1]).intValue());
            dp.setWeightLifted(((Number) row[2]).doubleValue());
            return dp;
        }).toList();

        dto.setDailyProgress(dailyList);
        return dto;
    }
    public WorkoutPlanDetailDTO getWorkoutPlanDetailById(Long planId, String trainerEmail) {
        User trainer = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Workout plan not found"));

        // ✅ Ensure the plan belongs to this trainer
        if (!plan.getTrainerId().equals(trainer.getId())) {
            throw new RuntimeException("Unauthorized: This plan does not belong to you");
        }

        List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanId(plan.getId());

        List<WorkoutPlanDetailDTO.ExerciseDetail> exerciseDetails = planExercises.stream().map(pe -> {
            Exercise exercise = exerciseRepository.findById(pe.getExerciseId())
                    .orElseThrow(() -> new RuntimeException("Exercise not found"));

            WorkoutPlanDetailDTO.ExerciseDetail detail = new WorkoutPlanDetailDTO.ExerciseDetail();
            detail.setPlanExerciseId(pe.getId());
            detail.setName(exercise.getName());
            detail.setDescription(exercise.getDescription());
            detail.setTargetMuscles(exercise.getTargetMuscles());
            detail.setSets(pe.getSets());
            detail.setReps(pe.getReps());
            detail.setRestTimeSeconds(pe.getRestTimeSeconds());
            detail.setDayOfWeek(pe.getDayOfWeek());
            return detail;
        }).toList();

        WorkoutPlanDetailDTO dto = new WorkoutPlanDetailDTO();
        dto.setPlanId(plan.getId());
        dto.setName(plan.getName());
        dto.setExercises(exerciseDetails);

        return dto;
    }



    @Transactional
    public void updatePlanExercise(String trainerEmail, Long planExerciseId, UpdatePlanExerciseDTO dto) {
        User trainer = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        PlanExercise pe = planExerciseRepository.findById(planExerciseId)
                .orElseThrow(() -> new RuntimeException("Plan exercise not found"));

        WorkoutPlan plan = workoutPlanRepository.findById(pe.getWorkoutPlanId())
                .orElseThrow(() -> new RuntimeException("Workout plan not found"));

        if (!plan.getTrainerId().equals(trainer.getId())) {
            throw new SecurityException("Not authorized to update this plan exercise");
        }

        pe.setSets(dto.getSets());
        pe.setReps(dto.getReps());
        pe.setRestTimeSeconds(dto.getRestTimeSeconds());
        pe.setDayOfWeek(dto.getDayOfWeek());

        planExerciseRepository.save(pe);
    }

    public List<WorkoutPlanClientDTO> getAllWorkoutPlansWithClients(String trainerEmail) {
        User trainer = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        // Fetch all workout plans for this trainer
        List<WorkoutPlan> plans = workoutPlanRepository.findByTrainerId(trainer.getId());

        return plans.stream().map(plan -> {
            // Find all client assignments for this plan
            List<ClientWorkoutAssignment> assignments =
                    clientWorkoutAssignmentRepository.findByWorkoutPlanId(plan.getId());

            List<WorkoutPlanClientDTO.ClientInfo> clientInfos = assignments.stream()
                    .map(a -> userRepository.findById(a.getClientId())
                            .map(client -> {
                                WorkoutPlanClientDTO.ClientInfo ci = new WorkoutPlanClientDTO.ClientInfo();
                                ci.setClientId(client.getId());
                                ci.setClientName(client.getName());
                                ci.setClientEmail(client.getEmail());
                                return ci;
                            })
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

            WorkoutPlanClientDTO dto = new WorkoutPlanClientDTO();
            dto.setPlanId(plan.getId());
            dto.setPlanName(plan.getName());
            dto.setClients(clientInfos);

            return dto;
        }).toList();
    }
    @Transactional
    public Feedback giveFeedback(String trainerEmail, Long clientId, String comments, int rating) {
        User trainer = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Feedback feedback = Feedback.builder()
                .trainerId(trainer.getId())
                .clientId(clientId)
                .comments(comments)
                .rating(rating)
                .givenAt(LocalDateTime.now())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        // ✅ Send notification to client
        String message = "You received new feedback from your trainer "
                + trainer.getName() + ": " + comments;
        notificationService.sendNotification(clientId, message);

        return saved;
    }

    public List<Feedback> getClientFeedback(Long clientId) {
        return feedbackRepository.findByClientId(clientId);
    }


}
