package Fitness_Management_System.Fitness.service;
import Fitness_Management_System.Fitness.dto.UserSummaryDTO;
import Fitness_Management_System.Fitness.model.User;
import Fitness_Management_System.Fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String createUser(String name, String email, String password, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return "Email already exists!";
        }
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .assignedTrainerId(null) // Admins/Trainers don't have an assigned trainer, Clients get assigned later
                .build();
        userRepository.save(user);
        return role + " account created successfully!";
    }

    public String deleteTrainer(Long trainerId) {
        Optional<User> userOpt = userRepository.findById(trainerId);
        if (userOpt.isEmpty()) {
            return "Trainer not found!";
        }
        User user = userOpt.get();
        if (!"TRAINER".equalsIgnoreCase(user.getRole())) {
            return "User is not a trainer!";
        }
        // Disassociate clients from this trainer before deleting
        List<User> clientsAssignedToTrainer = userRepository.findByAssignedTrainerId(trainerId);
        for (User client : clientsAssignedToTrainer) {
            client.setAssignedTrainerId(null); // Or reassign, depending on business logic
            userRepository.save(client);
        }
        userRepository.delete(user);
        return "Trainer deleted successfully!";
    }

    // New: Assign a Client to a Trainer (FR2.1)
    public String assignTrainerToClient(Long clientId, Long trainerId) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        Optional<User> trainerOpt = userRepository.findById(trainerId);

        if (clientOpt.isEmpty()) {
            return "Client not found with ID: " + clientId;
        }
        if (trainerOpt.isEmpty()) {
            return "Trainer not found with ID: " + trainerId;
        }

        User client = clientOpt.get();
        User trainer = trainerOpt.get();

        if (!"CLIENT".equalsIgnoreCase(client.getRole())) {
            return "User with ID " + clientId + " is not a CLIENT.";
        }
        if (!"TRAINER".equalsIgnoreCase(trainer.getRole())) {
            return "User with ID " + trainerId + " is not a TRAINER.";
        }

        client.setAssignedTrainerId(trainerId);
        userRepository.save(client);
        return "Client " + client.getName() + " assigned to Trainer " + trainer.getName() + " successfully!";
    }

    // Helper method for Admin to view all clients
    public List<User> getAllClients() {
        return userRepository.findByRole("CLIENT");
    }

    // Helper method for Admin to view all trainers
    public List<User> getAllTrainers() {
        return userRepository.findByRole("TRAINER");
    }

    public List<UserSummaryDTO> getAllUsersSummary() {
        return userRepository.findAll().stream()
                .map(user -> new UserSummaryDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getAssignedTrainerId()
                ))
                .collect(Collectors.toList());
    }
    // Delete Admin by ID
    public String deleteAdmin(Long adminId) {
        Optional<User> userOpt = userRepository.findById(adminId);
        if (userOpt.isEmpty()) {
            return "Admin not found!";
        }
        User user = userOpt.get();
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "User is not an admin!";
        }
        userRepository.delete(user);
        return "Admin deleted successfully!";
    }

    // Delete Client by ID
    public String deleteClient(Long clientId) {
        Optional<User> userOpt = userRepository.findById(clientId);
        if (userOpt.isEmpty()) {
            return "Client not found!";
        }
        User user = userOpt.get();
        if (!"CLIENT".equalsIgnoreCase(user.getRole())) {
            return "User is not a client!";
        }
        userRepository.delete(user);
        return "Client deleted successfully!";
    }

    // Modify assigned trainer for a client
    public String modifyAssignedTrainer(Long clientId, Long newTrainerId) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        Optional<User> trainerOpt = userRepository.findById(newTrainerId);

        if (clientOpt.isEmpty()) {
            return "Client not found with ID: " + clientId;
        }
        if (trainerOpt.isEmpty()) {
            return "Trainer not found with ID: " + newTrainerId;
        }

        User client = clientOpt.get();
        User trainer = trainerOpt.get();

        if (!"CLIENT".equalsIgnoreCase(client.getRole())) {
            return "User with ID " + clientId + " is not a CLIENT.";
        }
        if (!"TRAINER".equalsIgnoreCase(trainer.getRole())) {
            return "User with ID " + newTrainerId + " is not a TRAINER.";
        }

        client.setAssignedTrainerId(newTrainerId);
        userRepository.save(client);
        return "Trainer for client " + client.getName() + " modified to " + trainer.getName() + " successfully!";
    }

}
