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
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "mileage >= 0")
@Check(constraints = "mileage >= 0")
@Check(constraints =
        "make IN ('Toyota','Lexus','Hyundai','Nissan','Kia','Chevrolet','GMC','Ford','Mazda','Mitsubishi','Isuzu','Infiniti','Genesis') " +
                "AND model IN ('Land Cruiser','Prado','Camry','Corolla','Hilux','Yaris','Avalon','LX570','ES350','Sonata','Elantra','Tucson','Palisade','Altima','Patrol','X-Trail','Sportage','Sorento','Sentra','Tahoe','Suburban','Silverado','Yukon','Sierra','F-150','Explorer','CX-5','L200','D-Max') " +
                "AND year BETWEEN 1990 AND 2025")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String make;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String model;

    @Column(columnDefinition = "INT NOT NULL")
    private Integer year;

    @Column(columnDefinition = "VARCHAR(255)")
    private String nickname;

    @Column(columnDefinition = "INT NOT NULL")
    private Integer mileage;

    @Column(columnDefinition = "VARCHAR(17) UNIQUE")
    private String vin;

    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate purchaseDate;

    @Column(columnDefinition = "VARCHAR(4096)")
    private String registrationFileUrl;

    @Column(columnDefinition = "DATE")
    private LocalDate registrationExpiry;

    @Column(columnDefinition = "VARCHAR(4096)")
    private String insuranceFileUrl;

    @Column(columnDefinition = "DATE")
    private LocalDate insuranceEndDate;

    @Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
    private Boolean isAccessible;

    // Relations
    @ManyToOne
    @JsonIgnore
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "car" )
    private Set<Maintenance> maintenances;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "car" )
    private Set<Reminder> reminders;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "car")
    private Set<CarTransferRequest> transferRequests;

    // Timestamps
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
