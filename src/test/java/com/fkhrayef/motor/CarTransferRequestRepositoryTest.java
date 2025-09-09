package com.fkhrayef.motor;

import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.CarTransferRequest;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Repository.CarRepository;
import com.fkhrayef.motor.Repository.CarTransferRequestRepository;
import com.fkhrayef.motor.Repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CarTransferRequestRepositoryTest {

    @Autowired
    CarTransferRequestRepository transferRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    CarRepository carRepo;

    User user1, user2;
    Car car1, car2;
    CarTransferRequest carTransferRequest1, carTransferRequest2, carTransferRequest3;

    @BeforeEach
    void setUp() {
        // Users
        user1 = new User(null, "+966535347890", "Faisal", "faisal@example.com", "1234", "Riyadh", "USER", null, null, null, null, null, null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now());
        user2 = new User(null, "+966533129420", "Mshari", "mshari@example.com", "1234", "Riyadh", "USER", null, null, null, null, null, null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now());

        userRepo.save(user1);
        userRepo.save(user2);

        // Cars
        car1 = new Car(null, "Nissan", "Altima", 2022, "My Car", 7500, "DSL38FJDMR93JFKV2", LocalDate.of(2023, 7, 21), null, null, null, null, true, user1, null, null, null, LocalDateTime.now(), LocalDateTime.now());
        car2 = new Car(null, "Nissan", "Sentra", 2025, "Mshari's Car", 7500, "KKL38FJRRR93JFKV2", LocalDate.of(2025, 7, 21), null, null, null, null, true, user1, null, null, null, LocalDateTime.now(), LocalDateTime.now());

        carRepo.save(car1);
        carRepo.save(car2);

        // Requests
        carTransferRequest1 = new CarTransferRequest(null, "pending", car1, user1, user2, LocalDateTime.now(), LocalDateTime.now());
        carTransferRequest2 = new CarTransferRequest(null, "accepted", car2, user1, user2, LocalDateTime.now(), LocalDateTime.now());
        carTransferRequest3 = new CarTransferRequest(null, "pending", car1, user1, user2, LocalDateTime.now(), LocalDateTime.now());

        transferRepo.save(carTransferRequest1);
        transferRepo.save(carTransferRequest2);
        transferRepo.save(carTransferRequest3);
    }

    @Test
    public void findCarTransferRequestById() {
        CarTransferRequest found = transferRepo.findCarTransferRequestById(carTransferRequest1.getId());

        Assertions.assertEquals(found, carTransferRequest1);
    }

    @Test
    public void findAllByFromUser() {
        List<CarTransferRequest> list = transferRepo.findAllByFromUser_Id(user1.getId());
        Assertions.assertEquals(3, list.size());
    }

    @Test
    public void findAllByToUser() {
        List<CarTransferRequest> list = transferRepo.findAllByToUser_Id(user2.getId());
        Assertions.assertEquals(3, list.size());
    }

    @Test
    public void findAllByCar() {
        List<CarTransferRequest> list = transferRepo.findAllByCar_Id(car1.getId());
        Assertions.assertEquals(2, list.size());
    }

    @Test
    public void findByIdAndToUser() {
        CarTransferRequest ok = transferRepo.findByIdAndToUser_Id(carTransferRequest1.getId(), user2.getId());
        Assertions.assertNotNull(ok);

        CarTransferRequest wrong = transferRepo.findByIdAndToUser_Id(carTransferRequest1.getId(), user1.getId());
        Assertions.assertNull(wrong);
    }
}