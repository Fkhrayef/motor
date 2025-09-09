package com.fkhrayef.motor.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "offer_type IN ('accessories','insurance','services')")
@Check(constraints = "start_date <= end_date")
public class Marketing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Title can't be null")
    @Column(columnDefinition = "varchar(255) not null")
    private String title;

    @NotEmpty(message = "Offer type can't be null")
    @Pattern(regexp = "^(accessories|insurance|services)$", message = "Offer type must be one of: accessories, insurance, services")
    @Column(columnDefinition = "varchar(255) not null")
    private String offerType;

    @NotEmpty(message = "Description can't be null")
    @Column(columnDefinition = "text not null")
    private String description;

    @NotEmpty(message = "Poster url can't be null")
    @Column(columnDefinition = "varchar(4069) not null")
    @Size(max = 4069)
    private String posterUrl;

    @NotNull(message = "Start date can't be null")
    @Column(columnDefinition = "datetime not null")
    private LocalDateTime startDate;

    @NotNull(message = "End date can't be null")
    @Column(columnDefinition = "datetime not null")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
