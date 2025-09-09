package com.fkhrayef.motor.Repository;

import com.fkhrayef.motor.Model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog , Integer> {
}
