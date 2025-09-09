package com.fkhrayef.motor.DTOin;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceDTO {

    @NotEmpty(message = "recordType must not be Empty")
    @Pattern(regexp = "^(MAINTENANCE|ACCIDENT)$", message = "Record type must be either 'MAINTENANCE' or 'ACCIDENT'")
    private String recordType;

    @NotEmpty(message = "serviceType must not be Empty")
    private String serviceType;

    @NotNull(message = "serviceDate is required")
    @PastOrPresent(message = "serviceDate cannot be in the future")
    private LocalDate serviceDate;

    @NotNull(message = "mileage must be not null")
    @PositiveOrZero(message = "mileage must be greater than or equal to zero")
    private Integer mileage;

    private String notes;

}
