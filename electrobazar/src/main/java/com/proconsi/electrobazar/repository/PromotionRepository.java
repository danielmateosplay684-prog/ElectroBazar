package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Finds all active promotions that could be valid today.
     * Further filtering by date is done in the service isValid() check.
     */
    @Query("SELECT p FROM Promotion p WHERE p.active = true")
    List<Promotion> findAllActive();
}
