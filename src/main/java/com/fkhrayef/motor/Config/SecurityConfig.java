package com.fkhrayef.motor.Config;

import com.fkhrayef.motor.Service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(myUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(daoAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/users/register", "/docs", "/swagger-ui/**", "/v3/api-docs/**", "/api/v1/payments/webhook","/api/v1/payments/callback").permitAll()
                .requestMatchers("/api/v1/users/get", "/api/v1/cars/get", "/api/v1/cars/numbers/{userId}", "/api/v1/cars/{userId}/enforce-access", "/api/v1/car-ai/admin/**", "/api/v1/s3/**", "/api/v1/payments/payment/{paymentId}", "/api/v1/payments/subscription/expiring", "/api/v1/marketing/**", "/api/v1/maintenances/get", "/api/v1/reminders/get", "/api/v1/email/test", "/api/v1/transfer-requests/{id}", "/api/v1/transfer-requests/by-car/{carId}", "/api/v1/transfer-requests/by-status/{status}").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/users/update/{id}", "/api/v1/users/upload-license/{id}", "/api/v1/users/download-license/{id}", "/api/v1/users/delete-license/{id}", "/api/v1/users/{id}/subscription", "/api/v1/users/{id}/card", "/api/v1/car-ai/upload-manual/{carId}", "/api/v1/car-ai/ask/{carId}", "/api/v1/car-ai/car/{carId}/info", "/api/v1/cars/add", "/api/v1/cars/update/{id}", "/api/v1/cars/update/{carId}/mileage", "/api/v1/cars/delete/{id}", "/api/v1/cars/get/user", "/api/v1/cars/upload-registration/{id}", "/api/v1/cars/download-registration/{id}", "/api/v1/cars/delete-registration/{id}", "/api/v1/cars/upload-insurance/{id}", "/api/v1/cars/download-insurance/{id}", "/api/v1/cars/delete-insurance/{id}", "/api/v1/cars/visit-frequency/{make}/{model}", "/api/v1/cars/typical-mileage/{make}/{model}", "/api/v1/transfer-requests/{id}/accept", "/api/v1/transfer-requests/{id}/reject", "/api/v1/transfer-requests/{id}/cancel", "/api/v1/transfer-requests/incoming", "/api/v1/transfer-requests/outgoing", "/api/v1/transfer-requests/direct/{carId}/{toEmail}/{toPhone}", "/api/v1/maintenances/add/{carId}", "/api/v1/maintenances/update/{id}", "/api/v1/maintenances/delete/{id}", "/api/v1/maintenances/get/{carId}", "/api/v1/maintenances/upload-invoice/{id}", "/api/v1/maintenances/download-invoice/{id}", "/api/v1/maintenances/delete-invoice/{id}", "/api/v1/payments/card", "/api/v1/payments/subscription/user/{userId}/plan/{planType}/billing/{billingCycle}", "/api/v1/payments/status/{paymentId}", "/api/v1/payments/subscription/{userId}/cancel", "/api/v1/payments/subscription/{userId}/status", "/api/v1/reminders/add/{carId}", "/api/v1/reminders/update/{id}", "/api/v1/reminders/delete/{id}", "/api/v1/reminders/get/{carId}", "/api/v1/reminders/generate-maintenance/{carId}").hasAuthority("USER")
                .requestMatchers("/api/v1/auth/users/delete/{id}").hasAnyAuthority("USER", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .logout().logoutUrl("/api/v1/auth/logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .and()
                .httpBasic();

        return http.build();
    }
}
