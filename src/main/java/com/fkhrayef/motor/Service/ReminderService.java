package com.fkhrayef.motor.Service;

import com.fkhrayef.motor.Api.ApiException;
import com.fkhrayef.motor.DTOin.ReminderDTO;
import com.fkhrayef.motor.DTOout.MaintenanceReminderResponseDTO;
import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.Reminder;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Repository.CarRepository;
import com.fkhrayef.motor.Repository.ReminderRepository;
import com.fkhrayef.motor.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final CarRepository carRepository;
    private final RAGService ragService;
    private final WhatsAppService whatsappService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private void validateSubscription(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        if (user.getSubscription() == null || 
            user.getSubscription().getStatus() == null || 
            !"active".equalsIgnoreCase(user.getSubscription().getStatus())) {
            throw new ApiException("AI features require an active subscription. Please upgrade to Pro or Enterprise plan.");
        }

        String planType = user.getSubscription().getPlanType();
        if ((!"pro".equalsIgnoreCase(planType) && !"enterprise".equalsIgnoreCase(planType))) {
            throw new ApiException("AI features require an active subscription. Please upgrade to Pro or Enterprise plan.");
        }
    }

    public List<Reminder> getAllReminders(){
        return reminderRepository.findAll();
    }

    public void addReminder(Integer userId, Integer carId, ReminderDTO reminderDTO) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found");
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        if (Boolean.FALSE.equals(car.getIsAccessible())) {
            throw new ApiException("This car is not accessible on your current plan.");
        }

        Reminder reminder = new Reminder();

        reminder.setType(reminderDTO.getType());
        reminder.setDueDate(reminderDTO.getDueDate());
        reminder.setMessage(reminderDTO.getMessage());
        reminder.setIsSent(false);
        reminder.setCar(car);

        reminderRepository.save(reminder);
    }

    public void updateReminder(Integer userId, Integer id, ReminderDTO reminderDTO) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        Reminder reminder = reminderRepository.findReminderById(id);
        if (reminder == null) {
            throw new ApiException("Reminder not found");
        }

        if (!reminder.getCar().getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        Car car = reminder.getCar();
        if (Boolean.FALSE.equals(car.getIsAccessible())) {
            throw new ApiException("This car is not accessible on your current plan.");
        }

        reminder.setType(reminderDTO.getType());
        reminder.setDueDate(reminderDTO.getDueDate());
        reminder.setMessage(reminderDTO.getMessage());
        reminderRepository.save(reminder);
    }

    public void deleteReminder(Integer userId, Integer id) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        Reminder reminder = reminderRepository.findReminderById(id);
        if (reminder == null) {
            throw new ApiException("Reminder not found");
        }

        if (!reminder.getCar().getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        Car car = reminder.getCar();
        if (Boolean.FALSE.equals(car.getIsAccessible())) {
            throw new ApiException("This car is not accessible on your current plan.");
        }
        reminderRepository.delete(reminder);
    }

    public List<Reminder> getRemindersByCarId(Integer userId, Integer carId){
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("UNAUTHENTICATED USER");
        }

        Car car = carRepository.findCarById(carId);

        if (car == null){
            throw new ApiException("Car not found");
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        return reminderRepository.findRemindersByCarId(car.getId());
    }

    @Transactional
    public void generateAndSaveMaintenanceReminders(Integer userId, Integer carId) {
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("UNAUTHENTICATED USER");
        }

        // Validate user has active subscription for AI features
        validateSubscription(userId);

        // Find the car
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found");
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        if (Boolean.FALSE.equals(car.getIsAccessible())) {
            throw new ApiException("This car is not accessible on your current plan.");
        }

        // Mileage is required by RAG and cannot be null (Map.of forbids nulls)
        if (car.getMileage() == null) {
            throw new ApiException("Car mileage is required to generate maintenance reminders.");
        }

        // Generate document name from car details (same as /ask endpoint)
        String documentName = generateDocumentName(car);

        // Check if document exists in RAG system
        if (!ragService.documentExists(documentName)) {
            throw new ApiException("Manual for this car is not available. Please upload the manual first.");
        }

        // Call RAG API to get maintenance reminders
        MaintenanceReminderResponseDTO ragResponse = ragService.generateMaintenanceReminders(car.getMileage(), documentName);

        if (ragResponse == null || !ragResponse.getSuccess()) {
            throw new ApiException("Failed to generate maintenance reminders: " +
                (ragResponse != null ? ragResponse.getError() : "Unknown error"));
        }
        if (ragResponse.getReminders() == null || ragResponse.getReminders().isEmpty()) {
            throw new ApiException("RAG returned no maintenance reminders for this car/manual.");
        }

        // Convert RAG response to Reminder entities and save in batch
        List<Reminder> toSave = ragResponse.getReminders().stream()
                .map(reminderData -> {
                    Reminder reminder = new Reminder();
                    reminder.setType("maintenance");
                    reminder.setDueDate(LocalDate.parse(reminderData.getDueDate().trim()));
                    reminder.setMessage(reminderData.getMessage());
                    reminder.setMileage(reminderData.getMileage());
                    reminder.setPriority(reminderData.getPriority());
                    reminder.setCategory(reminderData.getCategory());
                    reminder.setCar(car);
                    reminder.setIsSent(false);
                    return reminder;
                })
                .filter(reminder -> {
                    // Check if reminder already exists (same car, type, dueDate, message)
                    return !reminderRepository.existsByCarAndTypeAndDueDateAndMessage(
                            car, "maintenance", reminder.getDueDate(), reminder.getMessage());
                })
                .collect(Collectors.toList());

        reminderRepository.saveAll(toSave);
    }

    // Helper method to generate document name from car details (same as CarAIService)
    private String generateDocumentName(Car car) {
        return String.format("%d %s %s owner-manual",
                car.getYear(),
                car.getMake(),
                car.getModel());
    }

    /**
     * Scheduled task to send reminder notifications
     * Runs daily at 9:00 AM to check for upcoming reminders
     */
    @Scheduled(cron = "0 * * * * *") // Every minute (for testing)
    public void sendReminderNotifications() {
        try {
            log.info("[Scheduler] Starting reminder notification check...");
            
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);
            LocalDate tomorrow = today.plusDays(1);
            
            // Fetch once
            List<Reminder> allReminders = reminderRepository.findAll();

            // Daily: due tomorrow (send regardless of isSent)
            List<Reminder> tomorrowReminders = allReminders.stream()
                    .filter(r -> r.getDueDate() != null)
                    .filter(r -> r.getDueDate().isEqual(tomorrow))
                    .collect(Collectors.toList());

            // Weekly: due in 2..7 days (exclude 'tomorrow' to avoid duplicate sends)
            List<Reminder> upcomingReminders = allReminders.stream()
                    .filter(r -> Boolean.FALSE.equals(r.getIsSent()))
                    .filter(r -> r.getDueDate() != null)
                    .filter(r -> r.getDueDate().isAfter(tomorrow))      // > tomorrow
                    .filter(r -> !r.getDueDate().isAfter(nextWeek))     // <= nextWeek
                    .collect(Collectors.toList());
            
            // Send notifications for reminders due in next week
            for (Reminder reminder : upcomingReminders) {
                try {
                    sendReminderNotification(reminder, "week");
                } catch (Exception e) {
                    log.error("[Scheduler] Failed to send weekly reminder notification for reminder ID {}: {}", 
                            reminder.getId(), e.getMessage());
                }
            }
            
            // Send notifications for reminders due tomorrow
            for (Reminder reminder : tomorrowReminders) {
                try {
                    sendReminderNotification(reminder, "day");
                } catch (Exception e) {
                    log.error("[Scheduler] Failed to send daily reminder notification for reminder ID {}: {}", 
                            reminder.getId(), e.getMessage());
                }
            }
            
            log.info("[Scheduler] Reminder notification check completed. Sent {} weekly and {} daily notifications.", 
                    upcomingReminders.size(), tomorrowReminders.size());
                    
        } catch (Exception e) {
            log.error("[Scheduler] Reminder notification job failed: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification for a specific reminder
     */
    private void sendReminderNotification(Reminder reminder, String notificationType) {
        Car car = reminder.getCar();
        if (car == null) return;
        
        User user = car.getUser();
        if (user == null) return;
        
        String message = buildReminderMessage(reminder, car, notificationType);

        if (notificationType.equals("day")) {
            // Send WhatsApp notification
            try {
                if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                    whatsappService.sendWhatsAppMessage(message, user.getPhone());
                    log.info("[Scheduler] WhatsApp notification sent to user {} for reminder ID {}",
                            user.getId(), reminder.getId());
                }
            } catch (Exception e) {
                log.error("[Scheduler] Failed to send WhatsApp notification: {}", e.getMessage());
            }
        } else {

            // Send Email notification
            try {
                if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                    String subject = "تذكير صيانة - " + car.getMake() + " " + car.getModel();
                    emailService.sendEmailHtml(user.getEmail(), subject, message);
                    log.info("[Scheduler] Email notification sent to user {} for reminder ID {}",
                            user.getId(), reminder.getId());
                }
            } catch (Exception e) {
                log.error("[Scheduler] Failed to send email notification: {}", e.getMessage());
            }
        }
        
        // Mark reminder as sent (only for weekly notifications)
        if (notificationType.equals("week")) {
            reminder.setIsSent(true);
            reminderRepository.save(reminder);
        }
    }
    
    /**
     * Build reminder message in Arabic (HTML format)
     */
    private String buildReminderMessage(Reminder reminder, Car car, String notificationType) {
        String timeFrame = notificationType.equals("day") ? "غداً" : "خلال الأسبوع القادم";
        String html = buildReminderHtml(reminder, car, timeFrame);
        return html;
    }
    
    // ================== HTML Templates ==================

    private String shell(String emoji, String title, String accentColor, String content) {
        return """
    <!DOCTYPE html>
    <html lang="ar" dir="rtl">
    <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <title>%s</title>
    </head>
    <body style="margin:0;background:#f6f7f9;font-family:Tahoma,Arial,sans-serif;line-height:1.9;color:#0f172a">
      <div style="max-width:600px;margin:24px auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:14px;overflow:hidden">
        
        <!-- Header -->
        <div style="background:%s;color:#fff;padding:16px 20px;display:flex;align-items:center;gap:10px">
          <div style="font-size:24px">%s</div>
          <div style="font-size:16px;font-weight:700"> %s </div>
          <div style="margin-inline-start:auto;font-size:14px;opacity:.9">Motor 🚗</div>
        </div>

        <!-- Body -->
        <div style="padding:22px">
          %s
          <div style="margin-top:18px;padding:12px 14px;border:1px dashed #e5e7eb;border-radius:10px;font-size:12px;color:#64748b">
            هذه رسالة تذكير آلية من تطبيق Motor.
          </div>
        </div>
      </div>
    </body>
    </html>
    """.formatted(title, accentColor, emoji, title, content);
    }

    private String buildReminderHtml(Reminder reminder, Car car, String timeFrame) {
        String content = """
        <p style="margin:0 0 10px;font-size:16px">مرحبًا %s 👋</p>

        <div style="background:#f0f9ff;border:1px solid #bae6fd;border-radius:12px;padding:14px 16px;margin:10px 0">
          <div style="font-weight:700;margin-bottom:6px">🔔 تذكير الصيانة</div>
          <div style="font-size:15px">📅 التاريخ:</div>
          <div style="font-size:20px;font-weight:800;margin-top:4px;letter-spacing:.3px">%s (%s)</div>
        </div>

        <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:12px 14px;margin-top:12px">
          <div style="font-weight:700;margin-bottom:8px">🚗 تفاصيل السيارة</div>
          <ul style="margin:0;padding:0 18px;color:#334155;font-size:14px">
            <li>🏷️ الماركة: %s</li>
            <li>🚘 الموديل: %s</li>
            <li>📆 السنة: %s</li>
          </ul>
        </div>

        <div style="background:#fef3c7;border:1px solid #fde68a;border-radius:12px;padding:12px 14px;margin-top:12px">
          <div style="font-weight:700;margin-bottom:8px">⚠️ تفاصيل التذكير</div>
          <ul style="margin:0;padding:0 18px;color:#334155;font-size:14px">
            <li>🔧 النوع: %s</li>
            <li>💬 الرسالة: %s</li>
            %s
            %s
            %s
          </ul>
        </div>

        <ul style="margin:14px 0 0;padding:0 18px;color:#334155;font-size:14px">
          <li>يرجى مراجعة جدول الصيانة والاستعداد للصيانة المطلوبة.</li>
        </ul>
    """.formatted(
            car.getUser().getName(),
            reminder.getDueDate(),
            timeFrame,
            car.getMake(),
            car.getModel(),
            car.getYear(),
            getReminderTypeInArabic(reminder.getType()),
            reminder.getMessage(),
            reminder.getMileage() != null ? "<li>🔢 الكيلومترات المستهدفة: " + reminder.getMileage() + "</li>" : "",
            reminder.getPriority() != null ? "<li>⭐ الأولوية: " + getPriorityInArabic(reminder.getPriority()) + "</li>" : "",
            reminder.getCategory() != null ? "<li>📂 الفئة: " + reminder.getCategory() + "</li>" : ""
        );

        return shell("🔔", "تذكير صيانة", "#3b82f6", content); // Blue
    }
    
    /**
     * Get reminder type in Arabic
     */
    private String getReminderTypeInArabic(String type) {
        if (type == null) return "غير معروف";
        switch (type) {
            case "maintenance":
                return "صيانة";
            case "license_expiry":
                return "انتهاء الرخصة";
            case "insurance_expiry":
                return "انتهاء التأمين";
            case "registration_expiry":
                return "انتهاء التسجيل";
            default:
                return type;
        }
    }
    
    /**
     * Get priority in Arabic
     */
    private String getPriorityInArabic(String priority) {
        if (priority == null) return "غير معروف";
        switch (priority.toLowerCase()) {
            case "high":
                return "عالي";
            case "medium":
                return "متوسط";
            case "low":
                return "منخفض";
            default:
                return priority;
        }
    }

    /**
     * Scheduled job: every Monday 9:00 AM send WhatsApp reminder
     * to update car mileage
     */
    @Scheduled(cron = "0 */10 * * * *") // Every 10 minutes (for testing)
    public void sendWeeklyMileageReminders() {
        log.info("[Scheduler] Starting weekly mileage reminders...");

        List<Car> cars = carRepository.findAll();
        for (Car car : cars) {
            if (car.getUser() == null) {
                continue;
            }

            User user = car.getUser();
            if (user.getPhone() == null || user.getPhone().isBlank()){
                continue;
            }

            String message = buildMileageReminderMessage(car);

            try {
                whatsappService.sendWhatsAppMessage(message, user.getPhone());
                log.info("[Scheduler] Weekly mileage reminder sent to user {} for car {}",
                        user.getId(), car.getId());
            } catch (Exception e) {
                log.error("[Scheduler] Failed to send weekly mileage reminder to user {}: {}",
                        user.getId(), e.getMessage());
            }
        }
    }

    private String buildMileageReminderMessage(Car car) {
        StringBuilder msg = new StringBuilder();
        msg.append("🚗 تذكير أسبوعي لتحديث عداد السيارة\n\n");
        msg.append("📋 تفاصيل السيارة:\n");
        msg.append("• الماركة: ").append(car.getMake()).append("\n");
        msg.append("• الموديل: ").append(car.getModel()).append("\n");
        msg.append("• السنة: ").append(car.getYear()).append("\n\n");
        msg.append("🔢 العداد الحالي المسجل: ").append(car.getMileage() != null ? car.getMileage() : "غير مسجل").append("\n\n");
        msg.append("💡 يرجى إدخال القراءة الجديدة للعداد عبر التطبيق للحفاظ على سجل الصيانة محدثاً.");
        return msg.toString();
    }

}
