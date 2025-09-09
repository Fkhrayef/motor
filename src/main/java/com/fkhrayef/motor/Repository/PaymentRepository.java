package com.fkhrayef.motor.Repository;

import com.fkhrayef.motor.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Integer> {
    Payment findPaymentById(Integer id);

    Payment findByMoyasarPaymentId(String moyasarPaymentId);
}
