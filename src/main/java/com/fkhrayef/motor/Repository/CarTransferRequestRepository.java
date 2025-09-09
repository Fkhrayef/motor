package com.fkhrayef.motor.Repository;

import com.fkhrayef.motor.Model.CarTransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarTransferRequestRepository extends JpaRepository<CarTransferRequest,Integer> {

    CarTransferRequest findCarTransferRequestById(Integer id);

    List<CarTransferRequest> findAllByFromUser_Id(Integer userId);
    List<CarTransferRequest> findAllByToUser_Id(Integer userId);

    List<CarTransferRequest> findAllByCar_Id(Integer carId);
    List<CarTransferRequest> findAllByStatus(String status);


    CarTransferRequest findByIdAndToUser_Id(Integer id, Integer toUserId);

    CarTransferRequest findByIdAndFromUser_Id(Integer id, Integer fromUserId);
}
