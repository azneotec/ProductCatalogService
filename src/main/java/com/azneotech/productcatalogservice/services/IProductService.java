package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.models.Product;

import java.util.List;

public interface IProductService {
    List<Product> getAllProducts();
    List<Product> getProductsByCategoryId(Long categoryId);
    List<Product> getProductsByCategoryName(String categoryName);
    List<Product> searchProducts(String query);
    Product getProductDetailsById(Long id);
    Product replaceProduct(Long id, Product product);
    Product createProduct(Product product);
    void deleteProduct(Long id);
}
