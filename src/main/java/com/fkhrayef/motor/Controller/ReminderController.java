package com.fkhrayef.motor.Controller;

import com.fkhrayef.motor.Api.ApiResponse;
import com.fkhrayef.motor.DTOin.ReminderDTO;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Service.ReminderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    // TODO: ADMIN
    @GetMapping("/get")
    public ResponseEntity<?> getAllReminders() {
        return ResponseEntity.status(HttpStatus.OK).body(reminderService.getAllReminders());
    }

    @PostMapping("/add/{carId}")
    public ResponseEntity<?> addReminder(@AuthenticationPrincipal User user, @PathVariable Integer carId, @Valid @RequestBody ReminderDTO reminderDTO) {
        reminderService.addReminder(user.getId(), carId, reminderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Reminder added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateReminder(@AuthenticationPrincipal User user, @PathVariable Integer id, @Valid @RequestBody ReminderDTO reminderDTO) {
        reminderService.updateReminder(user.getId(), id, reminderDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Reminder updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReminder(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        reminderService.deleteReminder(user.getId(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/get/{carId}")
    public ResponseEntity<?> getRemindersByCarId(@AuthenticationPrincipal User user, @PathVariable Integer carId) {
        return ResponseEntity.status(HttpStatus.OK).body(reminderService.getRemindersByCarId(user.getId(), carId));
    }

    @PostMapping("/generate-maintenance/{carId}")
    public ResponseEntity<?> generateMaintenanceReminders(@AuthenticationPrincipal User user, @PathVariable Integer carId) {
        reminderService.generateAndSaveMaintenanceReminders(user.getId(), carId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Maintenance reminders generated successfully"));
    }

}
