package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.Category;
import java.util.List;

/**
 * Interface defining operations for product category management.
 * Supports CRUD and filtering.
 */
public interface CategoryService {

    /**
     * Retrieves all categories, including inactive ones.
     * @return A list of all Category entities.
     */
    List<Category> findAll();

    /**
     * Filters categories by name or description based on a search string.
     * @param search The query string.
     * @return A filtered list of categories.
     */
    List<Category> getFilteredCategories(String search);

    /**
     * Filters categories with pagination and sorting support.
     */
    org.springframework.data.domain.Slice<Category> getFilteredCategories(String search, org.springframework.data.domain.Pageable pageable);

    /**
     * Retrieves all categories currently marked as active.
     * @return A list of active Category entities.
     */
    List<Category> findAllActive();

    /**
     * Finds a category by its ID.
     * @param id The primary key.
     * @return The found Category, or null if not found.
     */
    Category findById(Long id);

    /**
     * Persists a new category record.
     * @param category The entity to save.
     * @return The saved Category.
     */
    Category save(Category category);

    /**
     * Updates an existing category's information.
     * @param id       The ID of the category to update.
     * @param category The updated entity data.
     * @return The updated Category.
     */
    Category update(Long id, Category category);

    /**
     * Toggles the category active status.
     * @param id The ID of the category to toggle.
     */
    void toggleStatus(Long id);

    /**
     * Performs a permanent removal from the database.
     * Only allowed if no products are associated with the category.
     * @param id The ID of the category to remove.
     */
    void hardDelete(Long id);
}