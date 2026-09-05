package com.azneotech.productcatalogservice.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single image in a product's gallery.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductImage extends BaseModel {

    private String url;

    /** Alternative text, shown by screen readers and when the image fails to load. */
    private String altText;

    /** The image shown for this product in category listings. */
    private boolean primary;

    private int displayOrder;
}
