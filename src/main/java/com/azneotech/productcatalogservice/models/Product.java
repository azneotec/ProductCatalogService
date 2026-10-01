package com.azneotech.productcatalogservice.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Product extends BaseModel {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    private Float price;

    // No cascade: categories are created/managed independently via CategoryService,
    // so saving/deleting a product must never persist, merge, or remove its category.
    @ManyToOne
    @JsonManagedReference
    private Category category;

    // Hibernate leaves this null (not an empty Rating) when both columns are NULL.
    @Embedded
    private Rating rating;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Product product)) {
            return false;
        }
        return getId() != null && getId().equals(product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Product{id=" + getId() + ", title='" + title + "'}";
    }

}
