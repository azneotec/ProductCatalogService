package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.exceptions.CategoryException;
import com.azneotech.productcatalogservice.exceptions.CategoryExceptionType;
import com.azneotech.productcatalogservice.exceptions.ProductException;
import com.azneotech.productcatalogservice.exceptions.ProductExceptionType;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.repos.CategoryRepository;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import com.azneotech.productcatalogservice.search.IProductSearchService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StorageProductService implements IProductService {

    static final int MAX_SEARCH_RESULTS = 50;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IProductSearchService searchService;

    public StorageProductService(ProductRepository productRepository,
                                 CategoryRepository categoryRepository,
                                 IProductSearchService searchService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.searchService = searchService;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        // Resolve the category first so an unknown id is a 404 rather than an empty list.
        categoryRepository.findById(categoryId).orElseThrow(() -> new CategoryException(
                "Category with id " + categoryId + " doesn't exist", CategoryExceptionType.CATEGORY_NOT_FOUND));
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> getProductsByCategoryName(String categoryName) {
        Category category = categoryRepository.findFirstByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new CategoryException(
                        "Category with name '" + categoryName + "' doesn't exist",
                        CategoryExceptionType.CATEGORY_NOT_FOUND));
        return productRepository.findByCategoryId(category.getId());
    }

    @Override
    public List<Product> searchProducts(String query) {
        List<Long> ids = searchService.search(query, MAX_SEARCH_RESULTS);
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Product> productsById = productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        // Keep the index's relevance order; drop ids the index has but the DB no longer does.
        return ids.stream()
                .map(productsById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Product getProductDetailsById(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new ProductException("Product with id " + id + " doesn't exists", ProductExceptionType.PRODUCT_NOT_FOUND);
        }
        return productOptional.get();
    }

    @Override
    public Product replaceProduct(Long id, Product product) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new ProductException("Product with id " + id + " doesn't exists", ProductExceptionType.PRODUCT_NOT_FOUND);
        }

        product.setId(id);
        Product saved = productRepository.save(product);
        searchService.index(saved);
        return saved;
    }

    @Override
    public Product createProduct(Product product) {
        // Id is DB-generated on create, so it's always null here and there's nothing
        // to check for a pre-existing row; setting it manually would make save()
        // treat this as an update and overwrite whatever row already has that id.
        product.setId(null);
        Product saved = productRepository.save(product);
        searchService.index(saved);
        return saved;
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductException("Product with id " + id + " doesn't exists", ProductExceptionType.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
        searchService.remove(id);
    }
}
