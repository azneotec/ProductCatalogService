package com.azneotech.productcatalogservice.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Identity and audit fields shared by every model in the product-catalog.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseModel {

    private Long id;
    private Date createdAt;
    private Date lastUpdatedAt;
    private State state;

}
