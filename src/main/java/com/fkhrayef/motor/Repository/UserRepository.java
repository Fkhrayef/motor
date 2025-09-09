package com.fkhrayef.motor.Repository;

import com.fkhrayef.motor.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserById(Integer id);

    User findUserByEmailIgnoreCaseAndPhone(String email, String phone);

    User findUserByPhone(String phone); // Phone is our username!

    List<User> findByLicenseExpiry(LocalDate date);
}
