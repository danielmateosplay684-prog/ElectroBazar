package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.exception.DuplicateResourceException;
import com.proconsi.electrobazar.exception.ResourceNotFoundException;
import com.proconsi.electrobazar.model.Category;
import com.proconsi.electrobazar.repository.CategoryRepository;
import com.proconsi.electrobazar.repository.ProductRepository;
import com.proconsi.electrobazar.repository.specification.CategorySpecification;
import com.proconsi.electrobazar.service.ActivityLogService;
import com.proconsi.electrobazar.service.CategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link CategoryService}.
 * Provides standard CRUD and filtering logic using JPA Specifications.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;


    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllActive() {
        return categoryRepository.findByActiveTrueOrderByNameEsAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getFilteredCategories(String search) {
        Specification<Category> spec = CategorySpecification.filterCategories(search);
        return categoryRepository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Slice<Category> getFilteredCategories(String search, org.springframework.data.domain.Pageable pageable) {
        Specification<Category> spec = CategorySpecification.filterCategories(search);
        return categoryRepository.findSliceBy(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    @Override
    public Category save(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        if (categoryRepository.existsByNameEsIgnoreCase(category.getName())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre '" + category.getName() + "'.");
        }
        Category saved = categoryRepository.save(category);
        activityLogService.logActivity(
                "CREAR_CATEGORIA",
                "Nueva categoría creada: " + saved.getName(),
                "Admin",
                "CATEGORY",
                saved.getId());
        return saved;
    }

    @Override
    public Category update(Long id, Category updated) {
        if (updated.getName() == null || updated.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        Category existing = findById(id);

        if (!existing.getNameEs().equalsIgnoreCase(updated.getName())
                && categoryRepository.existsByNameEsIgnoreCase(updated.getName())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre '" + updated.getName() + "'.");
        }

        existing.setNameEs(updated.getName());
        existing.setDescriptionEs(updated.getDescription());
        existing.setActive(updated.getActive());
        
        Category saved = categoryRepository.save(existing);
        activityLogService.logActivity(
                "ACTUALIZAR_CATEGORIA",
                "Categoría actualizada: " + saved.getName(),
                "Admin",
                "CATEGORY",
                saved.getId());
        return saved;
    }

    @Override
    public void toggleStatus(Long id) {
        Category category = findById(id);
        category.setActive(!category.getActive());
        categoryRepository.save(category);

        activityLogService.logActivity(
                "TOGGLE_ESTADO_CATEGORIA",
                "Categoría " + (category.getActive() ? "activada" : "desactivada") + ": " + category.getName(),
                "Admin",
                "CATEGORY",
                category.getId());
    }

    @Override
    public void hardDelete(Long id) {
        Category category = findById(id);
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("No se puede eliminar una categoría que aún contiene productos (" + productCount + ").");
        }
        categoryRepository.delete(category);

        activityLogService.logActivity(
                "ELIMINAR_CATEGORIA_HARD",
                "Categoría eliminada permanentemente: " + category.getName(),
                "Admin",
                "CATEGORY",
                id);
    }
}