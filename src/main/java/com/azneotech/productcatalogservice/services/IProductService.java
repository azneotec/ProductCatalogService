package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.models.Product;

public interface IProductService {
    Product getProductDetailsById(Long id);
    Product replaceProduct(Long id, Product product);
}
