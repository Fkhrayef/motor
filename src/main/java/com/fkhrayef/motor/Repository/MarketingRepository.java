package com.fkhrayef.motor.Repository;

import com.fkhrayef.motor.Model.Marketing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingRepository extends JpaRepository<Marketing,Integer> {

    Marketing findMarketingById(Integer id);

}
