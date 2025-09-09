package com.fkhrayef.motor.Repository;

import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {
    Reminder findReminderById(Integer id);

    List<Reminder> findRemindersByCarId(Integer id);
    
    boolean existsByCarAndTypeAndDueDateAndMessage(Car car, String type, LocalDate dueDate, String message);
}
