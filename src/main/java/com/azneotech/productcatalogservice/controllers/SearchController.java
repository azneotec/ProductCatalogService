package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.ProductDto;
import com.azneotech.productcatalogservice.dtos.SearchRequestDto;
import com.azneotech.productcatalogservice.mappers.ProductMappers;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.services.JpaBasedSearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final JpaBasedSearchService searchService;

    public SearchController(JpaBasedSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping()
    public Page<Product> searchProducts(@RequestBody SearchRequestDto searchRequestDto) {
        return searchService.searchProducts(
                searchRequestDto.getQuery(),
                searchRequestDto.getPageSize(),
                searchRequestDto.getPageNumber(),
                searchRequestDto.getSortParams()
        );

//        List<Product> products = searchService.searchProducts(
//                searchRequestDto.getQuery(),
//                searchRequestDto.getPageSize(),
//                searchRequestDto.getPageNumber()
//        );

//        return ProductMappers.mapToProductDtos(products);
    }

}
