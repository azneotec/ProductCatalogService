package com.azneotech.productcatalogservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/** One element of {@code GET https://fakestoreapi.com/products}. */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FakeStoreProductDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Float price;
    private String image;
    private Rating rating;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rating {
        private Double rate;
        private Integer count;
    }
}
