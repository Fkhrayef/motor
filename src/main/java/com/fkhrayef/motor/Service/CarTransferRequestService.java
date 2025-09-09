package com.fkhrayef.motor.Service;

import com.fkhrayef.motor.Api.ApiException;
import com.fkhrayef.motor.DTOin.CarTransferResponseDTO;
import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.CarTransferRequest;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Repository.CarRepository;
import com.fkhrayef.motor.Repository.CarTransferRequestRepository;
import com.fkhrayef.motor.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class CarTransferRequestService {

    private final CarTransferRequestRepository transferRepo;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    private static final String PENDING = "pending";
    private static final String ACCEPTED = "accepted";
    private static final String REJECTED = "rejected";
    private static final String CANCELLED = "cancelled";


    private CarTransferResponseDTO toDto(CarTransferRequest r) {
        CarTransferResponseDTO dto = new CarTransferResponseDTO();
        dto.setId(r.getId());
        dto.setStatus(r.getStatus());
        dto.setCarId(r.getCar() != null ? r.getCar().getId() : null);
        dto.setFromUserId(r.getFromUser() != null ? r.getFromUser().getId() : null);
        dto.setToUserId(r.getToUser() != null ? r.getToUser().getId() : null);
        return dto;
    }

    public CarTransferResponseDTO directTransfer(Integer carId, Integer fromUserId, String toEmail, String toPhone) {
        User user = userRepository.findUserById(fromUserId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        if (carId == null || fromUserId == null || toEmail == null || toPhone == null) {
            throw new ApiException("carId و fromUserId و toEmail و toPhone مطلوبة");
        }

        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found");
        }

        User fromUser = userRepository.findUserById(fromUserId);
        if (fromUser == null){
            throw new ApiException("Sender user not found");
        }

        // نجيب المستقبل من الإيميل + الجوال
        User toUser = userRepository.findUserByEmailIgnoreCaseAndPhone(toEmail.trim(), toPhone.trim());
        if (toUser == null){
            throw new ApiException("Recipient data is invalid (email or phone)");
        }

        if (fromUser.getId().equals(toUser.getId())){
            throw new ApiException("Cannot create a transfer request to the same user");
        }
        // تأكيد أن السيارة مملوكة للمرسل الآن
        if (car.getUser() == null || !car.getUser().getId().equals(fromUser.getId())){
            throw new ApiException("You cannot create a transfer request for a car you do not own");
        }
        // (اختياري) منع تكرار طلب pending لنفس (car, from, to)
        boolean existsPending = transferRepo.findAllByFromUser_Id(fromUserId)
                .stream()
                .anyMatch(t ->
                        t.getCar() != null && t.getCar().getId().equals(carId) &&
                                t.getToUser() != null && t.getToUser().getId().equals(toUser.getId()) &&
                                PENDING.equals(t.getStatus())
                );
        if (existsPending) {
            throw new ApiException("A pending transfer request already exists for the same car and users");
        }

        // ✅ إنشـاء طلب نقل بحالة pending (بدون نقل ملكية)
        CarTransferRequest req = new CarTransferRequest();
        req.setCar(car);
        req.setFromUser(fromUser);
        req.setToUser(toUser);
        req.setStatus(PENDING);

        return toDto(transferRepo.save(req));
    }


    public CarTransferResponseDTO accept(Integer requestId, Integer actingUserId) {
        User user = userRepository.findUserById(actingUserId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        CarTransferRequest r = transferRepo.findCarTransferRequestById(requestId);
        if (r == null){
            throw new ApiException("Transfer request not found");
        }
        if (!PENDING.equals(r.getStatus())){
            throw new ApiException("Cannot accept a non-pending request");
        }

        if (transferRepo.findByIdAndToUser_Id(requestId,actingUserId) == null){
            throw new ApiException("Only the recipient can accept the request");
        }

        Car car = r.getCar();
        if (car.getUser() == null || !car.getUser().getId().equals(r.getFromUser().getId())){
            throw new ApiException("Car ownership changed before acceptance");
        }

        car.setUser(r.getToUser());
        carRepository.save(car);

        r.setStatus(ACCEPTED);
        transferRepo.save(r);

        return toDto(r);
    }

    public CarTransferResponseDTO cancel(Integer requestId , Integer actingUserId) {
        User user = userRepository.findUserById(actingUserId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        CarTransferRequest r = transferRepo.findCarTransferRequestById(requestId);
        if (r == null){
            throw new ApiException("Transfer request not found");
        }

        if (!PENDING.equals(r.getStatus())){
            throw new ApiException("Cannot cancel a non-pending request");
        }

        if (transferRepo.findByIdAndFromUser_Id(requestId, actingUserId) == null) {
            throw new ApiException("Only the requester can cancel the request");
        }

        r.setStatus(CANCELLED);
        transferRepo.save(r);

        return toDto(r);
    }

    public CarTransferResponseDTO reject(Integer requestId, Integer actingUserId) {
        User user = userRepository.findUserById(actingUserId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        CarTransferRequest r = transferRepo.findCarTransferRequestById(requestId);
        if (r == null){
            throw new ApiException("Transfer request not found");
        }

        if (!PENDING.equals(r.getStatus())){
            throw new ApiException("Cannot reject a non-pending request");
        }

        if (transferRepo.findByIdAndToUser_Id(requestId, actingUserId) == null){
            throw new ApiException("Only the recipient can reject the request");
        }
        r.setStatus(REJECTED);
        transferRepo.save(r);
        return toDto(r);
    }

    public CarTransferResponseDTO getOne(Integer id) {
        CarTransferRequest r = transferRepo.findCarTransferRequestById(id);
        if (r == null) {
            throw new ApiException("Transfer request not found");
        }
        return toDto(r);
    }

    public List<CarTransferResponseDTO> getIncoming(Integer toUserId){
        User user = userRepository.findUserById(toUserId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        return transferRepo.findAllByToUser_Id(toUserId).stream().map(this::toDto).toList();
    }

    public List<CarTransferResponseDTO> getOutgoing(Integer fromUserId) {
        User user = userRepository.findUserById(fromUserId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        return transferRepo.findAllByFromUser_Id(fromUserId).stream().map(this::toDto).toList();
    }

    public List<CarTransferResponseDTO> getByCar(Integer carId) {
        return transferRepo.findAllByCar_Id(carId).stream().map(this::toDto).toList();
    }

    public List<CarTransferResponseDTO> getByStatus(String status) {
        return transferRepo.findAllByStatus(status).stream().map(this::toDto).toList();
    }

}

