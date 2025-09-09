package com.fkhrayef.motor.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "status IN ('pending','accepted','rejected','cancelled')")
public class CarTransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(50) not null")
    private String status="pending";

    @ManyToOne
    @JsonIgnore
    private Car car;

    @ManyToOne
    @JsonIgnore
    private User fromUser;

    @ManyToOne
    @JsonIgnore
    private User toUser;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime requestedAt;

    @Column(name = "responded_at", columnDefinition = "datetime")
    private LocalDateTime respondedAt;


}
