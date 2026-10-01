package com.azneotech.productcatalogservice.repos;

import com.azneotech.productcatalogservice.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findAllByTitle(String title, Pageable pageable);
    Page<Product> findAllByTitleContainsIgnoreCase(String title, Pageable pageable);
}
