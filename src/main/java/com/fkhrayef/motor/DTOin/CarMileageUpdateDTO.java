package com.fkhrayef.motor.DTOin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarMileageUpdateDTO {

    @NotNull(message = "Mileage cannot be null")
    @PositiveOrZero(message = "Mileage must be >= 0")
    private Integer mileage;
}
