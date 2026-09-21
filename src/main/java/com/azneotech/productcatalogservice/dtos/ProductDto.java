package com.azneotech.productcatalogservice.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/** Wire shape for a product; field order mirrors FakeStore's payload. */
@Getter
@Setter
@JsonPropertyOrder({"id", "title", "price", "description", "category", "image", "rating"})
public class ProductDto {
    private Long id;
    private String title;
    private Float price;
    private String description;
    private CategoryDto category;
    private String image;
    private RatingDto rating;
}
