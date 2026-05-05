package com.proconsi.electrobazar.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface WorkerRepository extends JpaRepository<Worker, Long>, JpaSpecificationExecutor<Worker> {

    /**
     * Slice-based search to avoid COUNT(*) on worker list.
     */
    Slice<Worker> findSliceBy(Specification<Worker> spec, Pageable pageable);

    Optional<Worker> findByUsername(String username);
    Optional<Worker> findByEmail(String email);
    long countByRole_Id(Long roleId);
    List<Worker> findByRole_Id(Long roleId);
}
