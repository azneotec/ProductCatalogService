package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.models.Category;

import java.util.List;

public interface ICategoryService {
    List<Category> getAllCategories();

    Category createCategory(Category category);

    Category getCategoryById(Long id);
}
