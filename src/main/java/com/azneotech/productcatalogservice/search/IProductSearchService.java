package com.azneotech.productcatalogservice.search;

import com.azneotech.productcatalogservice.models.Product;

import java.util.Collection;
import java.util.List;

/**
 * Full-text index over products. Returns ids only: MySQL stays the source of
 * truth and callers hydrate the hits themselves, so an Elasticsearch-backed
 * implementation can replace the Lucene one without touching the service layer.
 */
public interface IProductSearchService {

    /** Upsert: replaces any existing document with the same product id. */
    void index(Product product);

    /** Idempotent; unknown ids are a no-op. */
    void remove(Long productId);

    /** Replaces the whole index with exactly these products. */
    void reindexAll(Collection<Product> products);

    /**
     * Product ids ordered by descending relevance. Blank input, or input that
     * analyses down to nothing (stop words, punctuation), yields an empty list.
     * Never throws because of the query text.
     */
    List<Long> search(String query, int limit);
}
