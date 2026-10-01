package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.SortParams;
import com.azneotech.productcatalogservice.dtos.SortType;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaBasedSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private JpaBasedSearchService searchService;

    private static SortParams sortParams(String criteria, SortType type) {
        SortParams params = new SortParams();
        params.setSortCriteria(criteria);
        params.setSortType(type);
        return params;
    }

    private Pageable searchAndCapturePageable(List<SortParams> sortParams) {
        when(productRepository.findAllByTitleContainsIgnoreCase(eq("shirt"), any(Pageable.class)))
                .thenReturn(new PageImpl<Product>(List.of()));

        searchService.searchProducts("shirt", 5, 2, sortParams);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAllByTitleContainsIgnoreCase(eq("shirt"), captor.capture());
        return captor.getValue();
    }

    @Test
    void testSearchProducts_WithNullSortParams_UsesUnsortedPageable() {
        Pageable pageable = searchAndCapturePageable(null);

        assertTrue(pageable.getSort().isUnsorted());
        assertEquals(5, pageable.getPageSize());
        assertEquals(2, pageable.getPageNumber());
    }

    @Test
    void testSearchProducts_WithEmptySortParams_UsesUnsortedPageable() {
        assertTrue(searchAndCapturePageable(List.of()).getSort().isUnsorted());
    }

    @Test
    void testSearchProducts_WithMultipleSortParams_BuildsOrdersInListOrder() {
        Pageable pageable = searchAndCapturePageable(List.of(
                sortParams("price", SortType.DESC),
                sortParams("id", SortType.ASC)));

        List<Sort.Order> orders = pageable.getSort().toList();
        assertEquals(2, orders.size());
        assertEquals(Sort.Order.desc("price"), orders.get(0));
        assertEquals(Sort.Order.asc("id"), orders.get(1));
    }

    @Test
    void testSearchProducts_WithNullSortType_DefaultsToAscending() {
        Pageable pageable = searchAndCapturePageable(List.of(sortParams("title", null)));

        assertEquals(Sort.Order.asc("title"), pageable.getSort().toList().get(0));
    }
}
