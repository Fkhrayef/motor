package com.fkhrayef.motor;

import com.fkhrayef.motor.Controller.CarTransferRequestController;
import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.CarTransferRequest;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Service.CarTransferRequestService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = CarTransferRequestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class CarTransferRequestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CarTransferRequestService transferService;

    User user1, user2;
    Car car1, car2;
    CarTransferRequest carTransferRequest1, carTransferRequest2;

    @BeforeEach
    void setup() {
        user1 = new User(1, "+966535347890", "Faisal", "faisal@example.com", "1234", "Riyadh", "USER",
                null, null, null, null, null, null, null, null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now());

        user2 = new User(2, "+966533129420", "Mshari", "mshari@example.com", "1234", "Riyadh", "USER",
                null, null, null, null, null, null, null, null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now());

        car1 = new Car(1, "Nissan", "Altima", 2022, "My Car", 7500, "DSL38FJDMR93JFKV2",
                LocalDate.of(2023, 7, 21), null, null, null, null, true,
                user1, null, null, null, LocalDateTime.now(), LocalDateTime.now());

        car2 = new Car(2, "Nissan", "Sentra", 2025, "Mshari's Car", 7500, "KKL38FJRRR93JFKV2",
                LocalDate.of(2025, 7, 21), null, null, null, null, true,
                user1, null, null, null, LocalDateTime.now(), LocalDateTime.now());

        carTransferRequest1 = new CarTransferRequest(1, "pending", car1, user1, user2,
                LocalDateTime.now(), null);
        carTransferRequest2 = new CarTransferRequest(2, "pending", car2, user1, user2,
                LocalDateTime.now(), null);
    }

    @Test
    void accept_returns200_emptyBody() throws Exception {
        when(transferService.accept(carTransferRequest1.getId(), user2.getId())).thenReturn(null);

        mockMvc.perform(post("/api/v1/transfer-requests/{id}/accept", carTransferRequest1.getId())
                        .with(user(user2)))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // null body
    }

    @Test
    void reject_returns200_emptyBody() throws Exception {
        when(transferService.reject(carTransferRequest2.getId(), user2.getId())).thenReturn(null);

        mockMvc.perform(post("/api/v1/transfer-requests/{id}/reject", carTransferRequest2.getId())
                        .with(user(user2)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void cancel_returns200_emptyBody() throws Exception {
        when(transferService.cancel(carTransferRequest1.getId(), user1.getId())).thenReturn(null);

        mockMvc.perform(post("/api/v1/transfer-requests/{id}/cancel", carTransferRequest1.getId())
                        .with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void byCar_returnsEmptyList() throws Exception {
        when(transferService.getByCar(car1.getId())).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/v1/transfer-requests/by-car/{carId}", car1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    void byStatus_returnsEmptyList() throws Exception {
        when(transferService.getByStatus("pending")).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/v1/transfer-requests/by-status/{status}", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }
}