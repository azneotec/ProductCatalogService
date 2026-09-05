package com.azneotech.productcatalogservice.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row of a product's specification table, for example "Screen Size" / "6.1 inch".
 */
@Getter
@Setter
@NoArgsConstructor
public class Specification extends BaseModel {

    private String name;
    private String value;

    /** Section this specification belongs to, for example "Display". May be null. */
    private String group;

    private int displayOrder;
}
