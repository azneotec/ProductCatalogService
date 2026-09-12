package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.models.Product;

import java.util.List;

public interface IProductService {
    List<Product> getAllProducts();
    Product getProductDetailsById(Long id);
    Product replaceProduct(Long id, Product product);
    Product createProduct(Product product);
}
