package com.azneotech.productcatalogservice.search;

import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rebuilds the search index from the database once the application is up.
 * {@link ApplicationReadyEvent} is published after every ApplicationRunner has
 * finished, so this always sees rows the seeder just inserted without any
 * explicit ordering. Turn it off (catalog.search.reindex-on-startup=false) for
 * a persistent index such as Elasticsearch.
 */
@Component
@ConditionalOnProperty(name = "catalog.search.reindex-on-startup", havingValue = "true", matchIfMissing = true)
public class ProductIndexBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexBootstrap.class);

    private final ProductRepository productRepository;
    private final IProductSearchService searchService;

    public ProductIndexBootstrap(ProductRepository productRepository, IProductSearchService searchService) {
        this.productRepository = productRepository;
        this.searchService = searchService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildIndex() {
        List<Product> products = productRepository.findAll();
        searchService.reindexAll(products);
        log.info("Product search index rebuilt with {} products", products.size());
    }
}
