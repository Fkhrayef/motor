package com.fkhrayef.motor.DTOout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceReminderResponseDTO {

    private Boolean success;
    private List<ReminderDataDTO> reminders;
    private String documentName;
    private Integer currentMileage;
    private String generatedAt;
    private String error;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReminderDataDTO {
        private String type;
        private String dueDate;
        private String message;
        private Integer mileage;
        private String priority;
        private String category;
    }
}
