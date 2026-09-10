package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
public class StorageProductService implements IProductService {

    private final ProductRepository productRepository;

    public StorageProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product getProductDetailsById(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            // TODO: use custom exception
            throw new RuntimeException("Product with id " + id + " doesn't exists");
        }
        return productOptional.get();
    }

    @Override
    public Product replaceProduct(Long id, Product product) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            // TODO: use custom exception
            throw new RuntimeException("Product with id " + id + " doesn't exists");
        }

        product.setId(id);
        return productRepository.save(product);
    }

    @Override
    public Product createProduct(Product product) {
        Optional<Product> productOptional = productRepository.findById(product.getId());
        if (productOptional.isPresent()) {
            // TODO: use custom exception
            throw new RuntimeException("Product with id " + product.getId() + " already exists");
        }

        return productRepository.save(product);
    }
}
