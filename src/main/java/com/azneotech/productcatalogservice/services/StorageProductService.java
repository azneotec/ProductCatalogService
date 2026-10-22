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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StorageProductService implements IProductService {

    static final int MAX_SEARCH_RESULTS = 1000;

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
    public Page<Product> searchProducts(String query, Pageable pageable) {
        List<Long> ids = searchService.search(query, MAX_SEARCH_RESULTS);
        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }
        if (pageable.getSort().isSorted()) {
            // An explicit sort replaces relevance order, so let the DB sort and page the hits.
            return productRepository.findByIdIn(ids, pageable);
        }
        // Relevance order lives only in the index: page over the ids, hydrate just that slice.
        int from = (int) Math.min(pageable.getOffset(), ids.size());
        int to = Math.min(from + pageable.getPageSize(), ids.size());
        List<Long> pageIds = ids.subList(from, to);
        if (pageIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, ids.size());
        }
        Map<Long, Product> productsById = productRepository.findAllById(pageIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        // Drop ids the index has but the DB no longer does.
        List<Product> content = pageIds.stream()
                .map(productsById::get)
                .filter(Objects::nonNull)
                .toList();
        return new PageImpl<>(content, pageable, ids.size());
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
