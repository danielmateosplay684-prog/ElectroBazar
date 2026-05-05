package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Category} entities.
 * Handles product classification and supports advanced filtering via JPA
 * Specifications.
 */
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    /**
     * Slice-based search for categories to avoid COUNT(*).
     */
    Slice<Category> findSliceBy(Specification<Category> spec, Pageable pageable);

    /**
     * Checks if a category with a given Spanish name exists, ignoring case
     * sensitivity.
     */
    boolean existsByNameEsIgnoreCase(String name);

    /**
     * Lists all active categories for display in the TPV interface (Spanish).
     */
    List<Category> findByActiveTrueOrderByNameEsAsc();

    /**
     * Finds a category by its exact Spanish name, ignoring case.
     */
    java.util.Optional<Category> findByNameEsIgnoreCase(String name);
}