package com.azneotech.productcatalogservice.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A node in the browsing tree, for example {@code Electronics > Phones > Android}.
 * A category holds its own products plus any sub categories beneath it.
 *
 * <p>Lombok's {@code @Data}/{@code @ToString}/{@code @EqualsAndHashCode} are deliberately
 * not used here: {@link #parent} and {@link #subCategories} form a cycle, so generated
 * implementations would recurse forever.
 */
@Getter
@Setter
@NoArgsConstructor
public class Category extends BaseModel {

    private String name;
    private String description;

    /** URL safe segment used to build browse links, for example {@code "phones"}. */
    private String slug;

    /** Artwork for the category tile shown while browsing. */
    private String imageUrl;

    private int displayOrder;

    /** Null for a root category. */
    private Category parent;

    private List<Category> subCategories = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    /** Adds a child category and keeps its parent link in step. */
    public void addSubCategory(Category subCategory) {
        if (subCategory == null) {
            return;
        }
        if (subCategories == null) {
            subCategories = new ArrayList<>();
        }
        subCategories.add(subCategory);
        subCategory.setParent(this);
    }

    /** Adds a product and keeps its category back reference in step. */
    public void addProduct(Product product) {
        if (product == null) {
            return;
        }
        if (products == null) {
            products = new ArrayList<>();
        }
        products.add(product);
        product.setCategory(this);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return subCategories == null || subCategories.isEmpty();
    }

    /**
     * Every product in this category and in all categories beneath it, so browsing
     * a parent category shows everything under it rather than only its direct products.
     */
    public List<Product> getAllProducts() {
        List<Product> all = new ArrayList<>();
        collectProducts(all);
        return all;
    }

    private void collectProducts(List<Product> collected) {
        if (products != null) {
            collected.addAll(products);
        }
        if (subCategories != null) {
            for (Category subCategory : subCategories) {
                subCategory.collectProducts(collected);
            }
        }
    }

    /**
     * The trail from the root category down to this one, for breadcrumbs.
     * The last element is always this category.
     */
    public List<Category> getPath() {
        List<Category> path = new ArrayList<>();
        for (Category current = this; current != null; current = current.getParent()) {
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Category category)) {
            return false;
        }
        return getId() != null && getId().equals(category.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Category{id=" + getId() + ", name='" + name + "', slug='" + slug + "'}";
    }
}
