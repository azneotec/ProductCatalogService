package com.azneotech.productcatalogservice.repos;

import com.azneotech.productcatalogservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // findFirst rather than findBy: name has no UNIQUE constraint and POST /categories
    // can create duplicates, which would make a plain findBy throw on the second one.
    Optional<Category> findFirstByNameIgnoreCase(String name);
}
