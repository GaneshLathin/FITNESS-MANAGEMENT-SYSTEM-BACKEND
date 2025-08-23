// ==============================
// ProgressLogRepository.java
// ==============================
package Fitness_Management_System.Fitness.repository;

import Fitness_Management_System.Fitness.model.ProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgressLogRepository extends JpaRepository<ProgressLog, Long> {

        List<ProgressLog> findByClientIdAndPlanExerciseIdOrderByLogTimeDesc(Long clientId, Long planExerciseId);

    List<ProgressLog> findByClientIdOrderByLogTimeDesc(Long clientId);

    // ✅ Today's completed reps for one exercise
    @Query("SELECT COALESCE(SUM(p.repsCompleted), 0) " +
            "FROM ProgressLog p " +
            "WHERE p.clientId = :clientId " +
            "AND p.planExerciseId = :planExerciseId " +
            "AND FUNCTION('DATE', p.logTime) = CURRENT_DATE")
    Integer getTotalRepsCompletedToday(@Param("clientId") Long clientId,
                                       @Param("planExerciseId") Long planExerciseId);

    // ✅ All-time completed reps
    @Query("SELECT COALESCE(SUM(p.repsCompleted), 0) " +
            "FROM ProgressLog p " +
            "WHERE p.clientId = :clientId " +
            "AND p.planExerciseId = :planExerciseId")
    Integer sumRepsCompleted(@Param("clientId") Long clientId,
                             @Param("planExerciseId") Long planExerciseId);


    @Query("SELECT COUNT(DISTINCT DATE(p.logTime)) " +
            "FROM ProgressLog p WHERE p.clientId = :clientId")
    int countDistinctDaysByClientId(@Param("clientId") Long clientId);

    @Query("SELECT COALESCE(SUM(p.weightLifted),0) " +
            "FROM ProgressLog p WHERE p.clientId = :clientId")
    double sumWeightLiftedByClientId(@Param("clientId") Long clientId);

    @Query("SELECT DATE(p.logTime), SUM(p.repsCompleted), SUM(p.weightLifted) " +
            "FROM ProgressLog p WHERE p.clientId = :clientId " +
            "GROUP BY DATE(p.logTime) ORDER BY DATE(p.logTime)")
    List<Object[]> getDailyProgress(Long clientId);

    @Query("SELECT e.name, SUM(p.repsCompleted), SUM(p.weightLifted) " +
            "FROM ProgressLog p JOIN PlanExercise pe ON p.planExerciseId = pe.id " +
            "JOIN Exercise e ON pe.exerciseId = e.id " +
            "WHERE p.clientId = :clientId " +
            "GROUP BY e.name")
    List<Object[]> getExerciseProgress(Long clientId);

    @Query("SELECT DATE(p.logTime), SUM(p.repsCompleted), SUM(p.weightLifted) " +
            "FROM ProgressLog p " +
            "WHERE p.clientId = :clientId AND p.planExerciseId = :planExerciseId " +
            "GROUP BY DATE(p.logTime) ORDER BY DATE(p.logTime)")
    List<Object[]> getDailyExerciseProgress(@Param("clientId") Long clientId,
                                            @Param("planExerciseId") Long planExerciseId);

}
