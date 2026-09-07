package com.azneotech.productcatalogservice.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Product extends BaseModel {

    private String title;
    private String description;
    private String imageUrl;
    private Double price;

    @ManyToOne(cascade = CascadeType.ALL)
    private Category category;

    private Boolean isSaleEligible;

    /*
    private String shortDescription;
    private String brand;
    private String sku;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private String currency;
    private List<ProductImage> images = new ArrayList<>();
    private List<Specification> specifications = new ArrayList<>();
    private Integer stockQuantity;
    private ProductStatus status;
    private Double averageRating;
    private Integer ratingCount;

    public void addImage(ProductImage image) {
        if (image == null) {
            return;
        }
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(image);
    }

    public void addSpecification(Specification specification) {
        if (specification == null) {
            return;
        }
        if (specifications == null) {
            specifications = new ArrayList<>();
        }
        specifications.add(specification);
    }

    public ProductImage getPrimaryImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .orElse(images.get(0));
    }

    public Map<String, List<Specification>> getSpecificationsByGroup() {
        Map<String, List<Specification>> grouped = new LinkedHashMap<>();
        if (specifications == null) {
            return grouped;
        }
        for (Specification specification : specifications) {
            String group = specification.getGroup();
            if (group == null || group.isBlank()) {
                group = "General";
            }
            grouped.computeIfAbsent(group, key -> new ArrayList<>()).add(specification);
        }
        return grouped;
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && stockQuantity != null && stockQuantity > 0;
    }

    public BigDecimal getEffectivePrice() {
        return discountedPrice != null ? discountedPrice : price;
    }
    */

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
//        return "Product{id=" + getId() + ", title='" + title + "', sku='" + sku + "'}";
        return "Product{id=" + getId() + ", title='" + title + "'}";
    }

}
