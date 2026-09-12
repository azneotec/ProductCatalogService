package com.azneotech.productcatalogservice.repos;

import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @Transactional
    public void testFetchTypesWithSingleCategoryAndProduct() {
        Optional<Category> categoryOptional = categoryRepository.findById(5000L);
        Category category = categoryOptional.orElse(null);
        for (Product product : category.getProducts()) {
            System.out.println(product.getTitle());
        }
    }

    @Test
    @Transactional
    public void testFetchTypesWithCategoryAndNotProduct() {
        Optional<Category> categoryOptional = categoryRepository.findById(5000L);
        Category category = categoryOptional.orElse(null);
        System.out.println(category.getName());
    }

    @Test
    @Transactional
    public void testFetchTypesWithMultipleCategoryAndProduct() {
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            System.out.println(category.getName());
            for (Product product : category.getProducts()) {
                System.out.println(product.getTitle());
            }
        }
    }

    @Test
    @Transactional
    public void testFetchTypesWithMultipleCategoryAndNotProduct() {
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            System.out.println(category.getName());
        }
    }

}
