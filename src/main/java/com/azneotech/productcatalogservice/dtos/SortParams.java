package com.azneotech.productcatalogservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortParams {
    private String sortCriteria;
    private SortType sortType;
}
