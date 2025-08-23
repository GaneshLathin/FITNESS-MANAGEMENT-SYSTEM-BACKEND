package Fitness_Management_System.Fitness.service;

import Fitness_Management_System.Fitness.model.PlanExercise;
import Fitness_Management_System.Fitness.repository.PlanExerciseRepository;
import Fitness_Management_System.Fitness.repository.ProgressLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlanExerciseService {

    @Autowired
    private PlanExerciseRepository planExerciseRepository;

    @Autowired
    private ProgressLogRepository progressLogRepository;

    public void updateCompletionStatus(Long clientId, Long planExerciseId) {
        PlanExercise planExercise = planExerciseRepository.findById(planExerciseId)
                .orElseThrow(() -> new RuntimeException("PlanExercise not found"));

        int totalRepsDone = progressLogRepository.sumRepsCompleted(clientId, planExerciseId);
        boolean isCompleted = totalRepsDone >= (planExercise.getSets() * planExercise.getReps());

        if (planExercise.isCompleted() != isCompleted) {
            planExercise.setCompleted(isCompleted);
            planExerciseRepository.save(planExercise);
        }
    }
}

