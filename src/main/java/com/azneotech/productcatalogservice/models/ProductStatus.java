package com.azneotech.productcatalogservice.models;

/**
 * Lifecycle state of a product, used to decide whether it surfaces while browsing.
 */
public enum ProductStatus {
    ACTIVE,
    OUT_OF_STOCK,
    DISCONTINUED
}
