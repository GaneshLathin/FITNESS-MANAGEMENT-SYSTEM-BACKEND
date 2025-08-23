package Fitness_Management_System.Fitness.controller;

import Fitness_Management_System.Fitness.model.Exercise;
import Fitness_Management_System.Fitness.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exercises")
@RequiredArgsConstructor
public class AdminExerciseController {

    private final ExerciseRepository exerciseRepository;

    // Create exercise
    @PostMapping
    public ResponseEntity<Exercise> create(@RequestBody Exercise req) {
        Exercise ex = new Exercise(); // ✅ create instance here
        ex.setName(req.getName());
        ex.setDescription(req.getDescription());
        ex.setTargetMuscles(req.getTargetMuscles());
        return ResponseEntity.ok(exerciseRepository.save(ex));
    }

    // Update exercise
    @PutMapping("/{id}")
    public ResponseEntity<Exercise> update(@PathVariable Long id, @RequestBody Exercise req) {
        Exercise ex1 = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));
        ex1.setName(req.getName());
        ex1.setDescription(req.getDescription());
        ex1.setTargetMuscles(req.getTargetMuscles());
        return ResponseEntity.ok(exerciseRepository.save(ex1));
    }

    // Delete exercise
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!exerciseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        exerciseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // List all exercises
    @GetMapping
    public List<Exercise> listAll() {
        return exerciseRepository.findAll();
    }
}
