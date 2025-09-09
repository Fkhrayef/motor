package com.fkhrayef.motor.DTOin;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReminderDTO {
    private Integer id;

    @NotEmpty(message = "Type can't be null")
    @Pattern(regexp = "^(license_expiry|insurance_expiry|registration_expiry|maintenance)$", message = "Type must be one of: license_expiry, insurance_expiry, registration_expiry, maintenance")
    private String type;

    @NotNull(message = "Due date can't be null")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @NotEmpty(message = "Message can't be null")
    private String message;

    //todo
//    @NotNull(message = "isSent can't be null")
//    private Boolean isSent;

}