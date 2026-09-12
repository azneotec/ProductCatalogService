package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.exceptions.FakeStoreApiExceptionType;
import com.azneotech.productcatalogservice.exceptions.FakeStoreApiProductException;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class StorageProductService implements IProductService {

    private final ProductRepository productRepository;

    public StorageProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductDetailsById(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new FakeStoreApiProductException("Product with id " + id + " doesn't exists", FakeStoreApiExceptionType.PRODUCT_NOT_FOUND);
        }
        return productOptional.get();
    }

    @Override
    public Product replaceProduct(Long id, Product product) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new FakeStoreApiProductException("Product with id " + id + " doesn't exists", FakeStoreApiExceptionType.PRODUCT_NOT_FOUND);
        }

        product.setId(id);
        return productRepository.save(product);
    }

    @Override
    public Product createProduct(Product product) {
        // Id is DB-generated on create, so it's always null here and there's nothing
        // to check for a pre-existing row; setting it manually would make save()
        // treat this as an update and overwrite whatever row already has that id.
        product.setId(null);
        return productRepository.save(product);
    }
}
