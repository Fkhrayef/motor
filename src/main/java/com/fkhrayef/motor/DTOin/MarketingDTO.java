package com.fkhrayef.motor.DTOin;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MarketingDTO {

    @NotNull(message = "Title can't be null")
    private String title;

    @NotEmpty(message = "Offer type can't be null")
    @Pattern(regexp = "^(accessories|insurance|services)$", message = "Offer type must be one of: accessories, insurance, services")
    private String offerType;

    @NotEmpty(message = "Description can't be null")
    private String description;

    @NotEmpty(message = "Poster url can't be null")
    @Size(max = 4069)
    private String posterUrl;

    @NotNull(message = "Start date can't be null")
    private LocalDateTime startDate;

    @NotNull(message = "End date can't be null")
    private LocalDateTime endDate;
}
