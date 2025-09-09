package com.fkhrayef.motor.DTOin;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class CarDTO {
    @NotEmpty(message = "Make cannot be null")
    @Pattern(
            regexp = "^(Toyota|Lexus|Hyundai|Nissan|Kia|Chevrolet|GMC|Ford|Mazda|Mitsubishi|Isuzu|Infiniti|Genesis)$",
            message = "Make must be a supported car brand e.g. Toyota, Honda, Ford"
    )
    private String make;

    @NotEmpty(message = "Model cannot be null")
    @Pattern(
            regexp = "^(Land Cruiser|Prado|Camry|Corolla|Hilux|Yaris|Avalon|LX570|ES350|Sonata|Elantra|Tucson|Palisade|Altima|Patrol|X-Trail|Sportage|Sorento|Sentra|Tahoe|Suburban|Silverado|Yukon|Sierra|F-150|Explorer|CX-5|L200|D-Max)$",
            message = "Model must be a popular model e.g. Corolla, Hilux, Yaris"
    )
    private String model;

    @NotNull(message = "Year cannot be null")
    @Min(value = 1990, message = "Year must be greater than or equal to 1990")
    @Max(value = 2025, message = "Year must be less than or equal to 2025")

    private Integer year;

    @NotEmpty(message = "Nickname cannot be null")
    private String nickname;

    @NotNull(message = "Mileage cannot be null")
    @PositiveOrZero(message = "mileage must be greater than or equal to zero")
    private Integer mileage;

    @NotEmpty(message = "VIN cannot be null")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    private String vin;

    @NotNull(message = "Purchase date cannot be null")
    private LocalDate purchaseDate;
}
