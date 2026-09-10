package com.azneotech.productcatalogservice.repos;

import com.azneotech.productcatalogservice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
//    Optional<Product> findById(Long id);
//    Product save(Product product);
}
