package com.fkhrayef.motor.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "mileage >= 0")
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(50) not null")
    private String recordType;

    @Column(columnDefinition = "varchar(50) not null")
    private String serviceType;

    @Column(columnDefinition = "date")
    private LocalDate serviceDate;

    @Column(columnDefinition = "int not null")
    private Integer mileage;

    private String notes;

    // Invoice (optional - can be added later via upload endpoint)
    @Column(columnDefinition = "VARCHAR(4096)")
    private String invoiceFileUrl;

    @Column(columnDefinition = "DOUBLE")
    private Double invoiceAmount;

    // Relations
    @ManyToOne
    @JsonIgnore
    private Car car;

    // Timestamps
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
