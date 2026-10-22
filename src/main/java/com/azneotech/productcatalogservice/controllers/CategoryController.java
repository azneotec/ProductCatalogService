package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.CategoryDto;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.services.IAuthorizationService;
import com.azneotech.productcatalogservice.services.ICategoryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final ICategoryService categoryService;
    private final IAuthorizationService authorizationService;

    public CategoryController(ICategoryService categoryService, IAuthorizationService authorizationService) {
        this.categoryService = categoryService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<CategoryDto[]> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        int n = categories.size();
        CategoryDto[] categoryDtos = new CategoryDto[n];
        for (int i = 0; i < n; i++) {
            categoryDtos[i] = mapToCategoryDto(categories.get(i));
        }
        return new ResponseEntity<>(categoryDtos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody CategoryDto categoryDto) {
        authorizationService.requireAdmin(authorization);
        Category category = categoryService.createCategory(mapToCategory(categoryDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToCategoryDto(category));
    }

    private Category mapToCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        return category;
    }

    private CategoryDto mapToCategoryDto(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }
}
