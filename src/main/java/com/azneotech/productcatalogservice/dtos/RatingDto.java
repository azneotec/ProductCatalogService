package com.azneotech.productcatalogservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingDto {
    private Double rate;
    private Integer count;
}
