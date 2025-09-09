package com.fkhrayef.motor.Repository;

import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Integer> {

    Maintenance findMaintenanceById(Integer id);

    List<Maintenance> findMaintenancesByCarId(Integer id);
}
