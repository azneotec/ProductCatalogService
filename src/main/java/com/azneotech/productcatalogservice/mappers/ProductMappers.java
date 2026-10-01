package com.azneotech.productcatalogservice.mappers;

import com.azneotech.productcatalogservice.dtos.CategoryDto;
import com.azneotech.productcatalogservice.dtos.ProductDto;
import com.azneotech.productcatalogservice.dtos.RatingDto;
import com.azneotech.productcatalogservice.models.Product;

import java.util.List;

public class ProductMappers {
    public static List<ProductDto> mapToProductDtos(List<Product> products) {
        return products.stream()
                .map(ProductMappers::mapToProductDto)
                .toList();
    }

    public static ProductDto mapToProductDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setTitle(product.getTitle());
        productDto.setPrice(product.getPrice());
        productDto.setDescription(product.getDescription());
        productDto.setImage(product.getImageUrl());

        if (product.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            productDto.setCategory(categoryDto);
        }
        if (product.getRating() != null) {
            RatingDto ratingDto = new RatingDto();
            ratingDto.setRate(product.getRating().getRate());
            ratingDto.setCount(product.getRating().getCount());
            productDto.setRating(ratingDto);
        }
        return productDto;
    }

}
