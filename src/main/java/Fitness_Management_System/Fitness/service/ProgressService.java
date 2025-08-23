// ==============================
// ProgressService.java
// ==============================
package Fitness_Management_System.Fitness.service;

import Fitness_Management_System.Fitness.dto.ExerciseProgressDTO;
import Fitness_Management_System.Fitness.model.Exercise;
import Fitness_Management_System.Fitness.model.PlanExercise;
import Fitness_Management_System.Fitness.model.ProgressLog;
import Fitness_Management_System.Fitness.repository.ExerciseRepository;
import Fitness_Management_System.Fitness.repository.PlanExerciseRepository;
import Fitness_Management_System.Fitness.repository.ProgressLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressLogRepository progressLogRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final ExerciseRepository exerciseRepository;

    /**
     * Save a progress log for a specific exercise in a workout plan.
     */
    public ProgressLog logExercise(Long clientId, Long planExerciseId, int repsCompleted, double weightLifted) {
        ProgressLog log = new ProgressLog();
        log.setClientId(clientId);
        log.setPlanExerciseId(planExerciseId);
        log.setRepsCompleted(repsCompleted);
        log.setWeightLifted(weightLifted);
        log.setLogTime(LocalDateTime.now());
        return progressLogRepository.save(log);
    }

    /**
     * Retrieve all logs for a specific exercise of a client (latest first).
     */
    public List<ProgressLog> getExerciseHistory(Long clientId, Long planExerciseId) {
        return progressLogRepository.findByClientIdAndPlanExerciseIdOrderByLogTimeDesc(clientId, planExerciseId);
    }

    /**
     * Retrieve all progress logs for a client (latest first).
     */
    public List<ProgressLog> getAllProgress(Long clientId) {
        return progressLogRepository.findByClientIdOrderByLogTimeDesc(clientId);
    }
    public ExerciseProgressDTO getExerciseProgress(Long clientId, Long planExerciseId) {
        // ✅ Lookup PlanExercise to get Exercise info
        PlanExercise planExercise = planExerciseRepository.findById(planExerciseId)
                .orElseThrow(() -> new RuntimeException("PlanExercise not found"));

        Exercise exercise = exerciseRepository.findById(planExercise.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        // ✅ Fetch daily progress logs grouped by date
        List<Object[]> rawData = progressLogRepository.getDailyExerciseProgress(clientId, planExerciseId);

        List<ExerciseProgressDTO.DailyProgress> dailyLogs = rawData.stream().map(r -> {
            ExerciseProgressDTO.DailyProgress dp = new ExerciseProgressDTO.DailyProgress();
            dp.setDate(((java.sql.Date) r[0]).toLocalDate());
            dp.setRepsCompleted(((Number) r[1]).intValue());
            dp.setWeightLifted(((Number) r[2]).doubleValue());
            return dp;
        }).toList();

        ExerciseProgressDTO dto = new ExerciseProgressDTO();
        dto.setExerciseId(exercise.getId());
        dto.setExerciseName(exercise.getName());
        dto.setDailyProgress(dailyLogs);

        return dto;
    }
}
