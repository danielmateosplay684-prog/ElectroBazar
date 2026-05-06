package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.Abono;
import com.proconsi.electrobazar.model.TipoAbono;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbonoRepository extends JpaRepository<Abono, Long> {
    List<Abono> findByClienteId(Long clienteId);
    Page<Abono> findByClienteId(Long clienteId, Pageable pageable);
    List<Abono> findByTipoAbono(TipoAbono tipoAbono);
    Optional<Abono> findByCode(String code);
    Optional<Abono> findTopByCodeStartingWithOrderByIdDesc(String prefix);

    boolean existsByClienteId(Long clienteId);
}

