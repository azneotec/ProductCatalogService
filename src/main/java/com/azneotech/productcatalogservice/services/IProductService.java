package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.models.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {
    List<Product> getAllProducts();
    List<Product> getProductsByCategoryId(Long categoryId);
    List<Product> getProductsByCategoryName(String categoryName);
    Page<Product> searchProducts(String query, Pageable pageable);
    Product getProductDetailsById(Long id);
    Product replaceProduct(Long id, Product product);
    Product createProduct(Product product);
    void deleteProduct(Long id);
}
