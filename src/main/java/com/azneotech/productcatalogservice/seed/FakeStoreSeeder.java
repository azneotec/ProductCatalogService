package com.azneotech.productcatalogservice.seed;

import com.azneotech.productcatalogservice.dtos.FakeStoreProductDto;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.models.Rating;
import com.azneotech.productcatalogservice.repos.CategoryRepository;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Populates an empty catalog from FakeStore on startup. Only writes to the
 * database; {@link com.azneotech.productcatalogservice.search.ProductIndexBootstrap}
 * picks the rows up afterwards.
 */
@Component
@ConditionalOnProperty(name = "catalog.seed.enabled", havingValue = "true", matchIfMissing = true)
public class FakeStoreSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FakeStoreSeeder.class);

    private final FakeStoreClient fakeStoreClient;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public FakeStoreSeeder(FakeStoreClient fakeStoreClient,
                           ProductRepository productRepository,
                           CategoryRepository categoryRepository) {
        this.fakeStoreClient = fakeStoreClient;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // One transaction: either all products land or none do, so a partial failure can't
    // trip the "already seeded" guard on the next boot.
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            log.info("Products already present, skipping FakeStore seed");
            return;
        }

        List<FakeStoreProductDto> fakeStoreProducts;
        try {
            fakeStoreProducts = fakeStoreClient.fetchAllProducts();
        } catch (RestClientException e) {
            // Network trouble must not fail boot; the catalog just stays empty.
            log.warn("FakeStore unreachable, skipping seed: {}", e.getMessage());
            return;
        }

        Map<String, Category> categoriesByName = new HashMap<>();
        List<Product> products = fakeStoreProducts.stream()
                .map(dto -> toProduct(dto, categoriesByName))
                .toList();
        productRepository.saveAll(products);
        log.info("Seeded {} products from FakeStore", products.size());
    }

    private Product toProduct(FakeStoreProductDto dto, Map<String, Category> categoriesByName) {
        Product product = new Product();
        // Id stays null: it's IDENTITY-generated, and a preset id would make save() merge.
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImage());
        if (dto.getRating() != null) {
            product.setRating(new Rating(dto.getRating().getRate(), dto.getRating().getCount()));
        }
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            product.setCategory(findOrCreateCategory(dto.getCategory(), categoriesByName));
        }
        return product;
    }

    // Categories are saved before their products because Product.category has no cascade.
    private Category findOrCreateCategory(String name, Map<String, Category> categoriesByName) {
        return categoriesByName.computeIfAbsent(name.toLowerCase(Locale.ROOT), key ->
                categoryRepository.findFirstByNameIgnoreCase(name).orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    return categoryRepository.save(category);
                }));
    }
}
