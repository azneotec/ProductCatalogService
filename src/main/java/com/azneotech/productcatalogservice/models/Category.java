package com.azneotech.productcatalogservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Category extends BaseModel {

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
//    @Fetch(FetchMode.SELECT)
//    @BatchSize(size = 10)
    private List<Product> products;

    /*
    private String slug;
    private String imageUrl;
    private int displayOrder;
    private Category parent;
    private List<Category> subCategories = new ArrayList<>();

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

    public List<Category> getPath() {
        List<Category> path = new ArrayList<>();
        for (Category current = this; current != null; current = current.getParent()) {
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    */

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
//        return "Category{id=" + getId() + ", name='" + name + "', slug='" + slug + "'}";
        return "Category{id=" + getId() + ", name='" + name + "'}";
    }
}
