package com.fkhrayef.motor.Repository;

import com.fkhrayef.motor.Model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {
    Car findCarById(Integer id);

    Car findCarByIdAndUserId(Integer carId , Integer userId);

    List<Car> findCarsByUserId(Integer id);

    List<Car> findByMakeAndModel(String make, String model);

    long countByUserId(Integer userId);

    List<Car> findByUserIdOrderByCreatedAtAsc(Integer userId);

    List<Car> findByRegistrationExpiry(LocalDate date);
    List<Car> findByInsuranceEndDate(LocalDate date);
}
