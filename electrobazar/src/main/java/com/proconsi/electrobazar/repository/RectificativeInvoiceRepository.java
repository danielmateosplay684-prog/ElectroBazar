package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.RectificativeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RectificativeInvoiceRepository extends JpaRepository<RectificativeInvoice, Long> {

    Optional<RectificativeInvoice> findBySaleReturnId(Long returnId);

}
