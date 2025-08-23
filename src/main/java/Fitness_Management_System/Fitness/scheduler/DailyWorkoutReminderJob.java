package Fitness_Management_System.Fitness.scheduler;

import Fitness_Management_System.Fitness.repository.UserRepository;
import Fitness_Management_System.Fitness.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyWorkoutReminderJob {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Run every day at 7 AM
    @Scheduled(cron = "0 0 7 * * ?")
    public void sendDailyReminders() {
        userRepository.findAll().stream()
                .filter(u -> "CLIENT".equalsIgnoreCase(u.getRole()))
                .forEach(client -> notificationService.sendNotification(
                        client.getId(),
                        "Reminder: Don't forget to complete your workout today!"
                ));
    }
}
