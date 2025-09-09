package com.fkhrayef.motor.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "role IN ('ADMIN','USER')")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "VARCHAR(13) NOT NULL UNIQUE")
    private String phone;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String name;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL UNIQUE")
    private String email;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String password;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String city;

    @Pattern(regexp = "^(ADMIN|USER)$", message = "Role must be either 'ADMIN' or 'USER'")
    private String role;

    // License Information (Optional)
    @Column(columnDefinition = "VARCHAR(4096)")
    private String licenseFileUrl;
    @Column(columnDefinition = "DATE")
    private LocalDate licenseExpiry;

    // Card Information (Optional but required for subscription)
    @Column(columnDefinition = "VARCHAR(100)")
    private String cardName;
    @Column(columnDefinition = "VARCHAR(32)")
    private String cardNumber;
    @Column(columnDefinition = "VARCHAR(8)")
    private String cardCvc;
    @Column(columnDefinition = "VARCHAR(4)")
    private String cardExpMonth;
    @Column(columnDefinition = "VARCHAR(6)")
    private String cardExpYear;

    // Relations
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Car> cars;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Payment> payments;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    @PrimaryKeyJoinColumn
    private Subscription subscription;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fromUser")
    private Set<CarTransferRequest> sentTransferRequests;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "toUser")
    private Set<CarTransferRequest> receivedTransferRequests;

    // Timestamps
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getUsername() {
        return this.phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
