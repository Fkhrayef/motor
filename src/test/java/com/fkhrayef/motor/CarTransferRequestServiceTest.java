package com.fkhrayef.motor;

import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.CarTransferRequest;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Repository.CarRepository;
import com.fkhrayef.motor.Repository.CarTransferRequestRepository;
import com.fkhrayef.motor.Repository.UserRepository;
import com.fkhrayef.motor.Service.CarTransferRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarTransferRequestServiceTest {

    @InjectMocks
    CarTransferRequestService carTransferRequestService;

    @Mock
    CarTransferRequestRepository transferRepo;
    @Mock
    CarRepository carRepository;
    @Mock
    UserRepository userRepository;

    User user1, user2;
    Car car1, car2;
    CarTransferRequest carTransferRequest1, carTransferRequest2;
    List<Car> cars;
    List<CarTransferRequest> carTransferRequests;

    @BeforeEach
    void setup() {
        user1 = new User(1, "+966535347890", "Faisal", "faisal@example.com", "1234", "Riyadh", "USER", null, null, null, null, null, null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now());
        user2 = new User(2, "+966533129420", "Mshari", "mshari@example.com", "1234", "Riyadh", "USER", null, null, null, null, null, null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now());

        car1 = new Car(1, "Nissan", "Altima", 2022, "My Car", 7500, "DSL38FJDMR93JFKV2", LocalDate.of(2023, 7, 21), null, null, null, null, true, user1, null, null, null, LocalDateTime.now(), LocalDateTime.now());
        car2 = new Car(2, "Nissan", "Sentra", 2025, "Mshari's Car", 7500, "KKL38FJRRR93JFKV2", LocalDate.of(2025, 7, 21), null, null, null, null, true, user1, null, null, null, LocalDateTime.now(), LocalDateTime.now());

        cars = List.of(car1, car2);

        carTransferRequest1 = new CarTransferRequest(1, "PENDING", car1, user1, user2, LocalDateTime.now(), LocalDateTime.now());
        carTransferRequest2 = new CarTransferRequest(2, "PENDING", car2, user1, user2, LocalDateTime.now(), LocalDateTime.now());

        carTransferRequests = List.of(carTransferRequest1, carTransferRequest2);
    }

    @Test
    public void getOutgoingTest() {
        // Given
        when(userRepository.findUserById(user1.getId())).thenReturn(user1);
        when(transferRepo.findAllByFromUser_Id(user1.getId())).thenReturn(carTransferRequests);

        // When
        var result = carTransferRequestService.getOutgoing(user1.getId());

        // Then
        Assertions.assertEquals(2, result.size());
        verify(userRepository, times(1)).findUserById(user1.getId());
        verify(transferRepo, times(1)).findAllByFromUser_Id(user1.getId());
    }

    @Test
    public void directTransferSuccessTest() {
        // Given
        Integer carId = car1.getId();
        Integer fromUserId = user1.getId();
        String toEmail = user2.getEmail();
        String toPhone = user2.getPhone();

        when(userRepository.findUserById(fromUserId)).thenReturn(user1);
        when(carRepository.findCarById(carId)).thenReturn(car1);
        when(userRepository.findUserByEmailIgnoreCaseAndPhone(toEmail, toPhone)).thenReturn(user2);
        when(transferRepo.findAllByFromUser_Id(fromUserId)).thenReturn(List.of());

        when(transferRepo.save(any(CarTransferRequest.class))).thenAnswer(invocation -> {
            CarTransferRequest saved = invocation.getArgument(0);
            saved.setId(99);
            return saved;
        });

        // When
        var dto = carTransferRequestService.directTransfer(carId, fromUserId, toEmail, toPhone);

        // Then
        Assertions.assertEquals(99, dto.getId());
        Assertions.assertEquals("pending", dto.getStatus());
        Assertions.assertEquals(carId, dto.getCarId());
        Assertions.assertEquals(fromUserId, dto.getFromUserId());
        Assertions.assertEquals(user2.getId(), dto.getToUserId());

        verify(userRepository, times(2)).findUserById(fromUserId);
        verify(carRepository, times(1)).findCarById(carId);
        verify(userRepository, times(1)).findUserByEmailIgnoreCaseAndPhone(toEmail, toPhone);
        verify(transferRepo, times(1)).findAllByFromUser_Id(fromUserId);
        verify(transferRepo, times(1)).save(any(CarTransferRequest.class));
    }

    @Test
    public void acceptSuccessTest() {
        // Given
        carTransferRequest1.setStatus("pending");
        car1.setUser(user1);

        when(userRepository.findUserById(user2.getId())).thenReturn(user2);
        when(transferRepo.findCarTransferRequestById(carTransferRequest1.getId())).thenReturn(carTransferRequest1);
        when(transferRepo.findByIdAndToUser_Id(carTransferRequest1.getId(), user2.getId())).thenReturn(carTransferRequest1);

        // When
        var dto = carTransferRequestService.accept(carTransferRequest1.getId(), user2.getId());

        // Then
        Assertions.assertEquals("accepted", dto.getStatus());
        Assertions.assertEquals(user2.getId(), car1.getUser().getId());
        verify(userRepository, times(1)).findUserById(user2.getId());
        verify(transferRepo, times(1)).findCarTransferRequestById(carTransferRequest1.getId());
        verify(transferRepo, times(1)).findByIdAndToUser_Id(carTransferRequest1.getId(), user2.getId());
        verify(carRepository, times(1)).save(car1);
        verify(transferRepo, times(1)).save(carTransferRequest1);
    }

    @Test
    public void cancelSuccessTest() {
        // Given
        carTransferRequest1.setStatus("pending");

        when(userRepository.findUserById(user1.getId())).thenReturn(user1);
        when(transferRepo.findCarTransferRequestById(carTransferRequest1.getId())).thenReturn(carTransferRequest1);
        when(transferRepo.findByIdAndFromUser_Id(carTransferRequest1.getId(), user1.getId())).thenReturn(carTransferRequest1);

        // When
        var dto = carTransferRequestService.cancel(carTransferRequest1.getId(), user1.getId());

        // Then
        Assertions.assertEquals("cancelled", dto.getStatus());
        verify(userRepository, times(1)).findUserById(user1.getId());
        verify(transferRepo, times(1)).findCarTransferRequestById(carTransferRequest1.getId());
        verify(transferRepo, times(1)).findByIdAndFromUser_Id(carTransferRequest1.getId(), user1.getId());
        verify(transferRepo, times(1)).save(carTransferRequest1);
    }

    @Test
    public void rejectSuccessTest() {
        // Given
        carTransferRequest1.setStatus("pending");

        when(userRepository.findUserById(user2.getId())).thenReturn(user2);
        when(transferRepo.findCarTransferRequestById(carTransferRequest1.getId())).thenReturn(carTransferRequest1);
        when(transferRepo.findByIdAndToUser_Id(carTransferRequest1.getId(), user2.getId())).thenReturn(carTransferRequest1);

        // When
        var dto = carTransferRequestService.reject(carTransferRequest1.getId(), user2.getId());

        // Then
        Assertions.assertEquals("rejected", dto.getStatus());
        verify(userRepository, times(1)).findUserById(user2.getId());
        verify(transferRepo, times(1)).findCarTransferRequestById(carTransferRequest1.getId());
        verify(transferRepo, times(1)).findByIdAndToUser_Id(carTransferRequest1.getId(), user2.getId());
        verify(transferRepo, times(1)).save(carTransferRequest1);
    }

}
