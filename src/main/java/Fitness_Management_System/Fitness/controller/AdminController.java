package Fitness_Management_System.Fitness.controller;
import Fitness_Management_System.Fitness.dto.AssignTrainerRequest;
import Fitness_Management_System.Fitness.dto.RegisterRequest;
import Fitness_Management_System.Fitness.dto.UserSummaryDTO;
import Fitness_Management_System.Fitness.model.User;
import Fitness_Management_System.Fitness.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    public AdminService adminService;

    // Create another Admin
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(@RequestBody RegisterRequest request) {
        String result = adminService.createUser(request.getName(), request.getEmail(), request.getPassword(), "ADMIN");
        return ResponseEntity.ok(result);
    }

    // Create Trainer
    @PostMapping("/create-trainer")
    public ResponseEntity<String> createTrainer(@RequestBody RegisterRequest request) {
        String result = adminService.createUser(request.getName(), request.getEmail(), request.getPassword(), "TRAINER");
        return ResponseEntity.ok(result);
    }

    // Delete Trainer
    @DeleteMapping("/delete-trainer/{id}")
    public ResponseEntity<String> deleteTrainer(@PathVariable Long id) {
        String result = adminService.deleteTrainer(id);
        return ResponseEntity.ok(result);
    }

    // New: Assign a Client to a Trainer (FR2.1)
    @PostMapping("/assign-trainer")
    public ResponseEntity<String> assignTrainer(@RequestBody AssignTrainerRequest request) {
        String result = adminService.assignTrainerToClient(request.getClientId(), request.getTrainerId());
        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    // FR9.1: View all users with roles
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsersSummary());
    }


    // Helper method for Admin to view all clients
    @GetMapping("/all-clients")
    public List<User> getAllClients() {
        return adminService.getAllClients();
    }

    // Helper method for Admin to view all trainers
    @GetMapping("/all-trainers")
    public List<User> getAllTrainers() {
        return adminService.getAllTrainers();
    }

    // Delete Admin
    @DeleteMapping("/delete-admin/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {
        String result = adminService.deleteAdmin(id);
        return ResponseEntity.ok(result);
    }

    // Delete Client
    @DeleteMapping("/delete-client/{id}")
    public ResponseEntity<String> deleteClient(@PathVariable Long id) {
        String result = adminService.deleteClient(id);
        return ResponseEntity.ok(result);
    }

    // Modify Assigned Trainer for a Client
    @PutMapping("/modify-assigned-trainer")
    public ResponseEntity<String> modifyAssignedTrainer(@RequestBody AssignTrainerRequest request) {
        String result = adminService.modifyAssignedTrainer(request.getClientId(), request.getTrainerId());
        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

}
