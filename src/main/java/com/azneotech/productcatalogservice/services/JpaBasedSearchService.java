package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.SortParams;
import com.azneotech.productcatalogservice.dtos.SortType;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JpaBasedSearchService {

    private final ProductRepository productRepository;

    public JpaBasedSearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> searchProducts(
            String query,
            Integer pageSize,
            Integer pageNumber,
            List<SortParams> sortParams
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, buildSort(sortParams));
        return productRepository.findAllByTitleContainsIgnoreCase(query, pageable);
    }

    // List order is sort priority; no params means unsorted, a missing sortType means ASC.
    private Sort buildSort(List<SortParams> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = sortParams.stream()
                .map(p -> new Sort.Order(
                        p.getSortType() == SortType.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        p.getSortCriteria()))
                .toList();
        return Sort.by(orders);
    }
}
