package com.azneotech.productcatalogservice.repos;

import com.azneotech.productcatalogservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
