package com.azneotech.productcatalogservice.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The top level container users enter when they start browsing. Holds the root
 * categories; everything else hangs off those.
 */
@Getter
@Setter
@NoArgsConstructor
public class Catalog extends BaseModel {

    private String name;
    private String description;
    private List<Category> rootCategories = new ArrayList<>();

//    /** Adds a top level category, detaching it from any previous parent. */
//    public void addRootCategory(Category category) {
//        if (category == null) {
//            return;
//        }
//        if (rootCategories == null) {
//            rootCategories = new ArrayList<>();
//        }
//        rootCategories.add(category);
//        category.setParent(null);
//    }
//
//    /** The whole category tree flattened depth first, roots before their children. */
//    public List<Category> getAllCategories() {
//        List<Category> all = new ArrayList<>();
//        if (rootCategories != null) {
//            for (Category rootCategory : rootCategories) {
//                collectCategories(rootCategory, all);
//            }
//        }
//        return all;
//    }
//
//    private void collectCategories(Category category, List<Category> collected) {
//        collected.add(category);
//        List<Category> subCategories = category.getSubCategories();
//        if (subCategories != null) {
//            for (Category subCategory : subCategories) {
//                collectCategories(subCategory, collected);
//            }
//        }
//    }
//
//    /** Resolves a browse URL segment to its category. */
//    public Optional<Category> findCategoryBySlug(String slug) {
//        if (slug == null) {
//            return Optional.empty();
//        }
//        return getAllCategories().stream()
//                .filter(category -> slug.equals(category.getSlug()))
//                .findFirst();
//    }
//
//    public Optional<Category> findCategoryById(Long id) {
//        if (id == null) {
//            return Optional.empty();
//        }
//        return getAllCategories().stream()
//                .filter(category -> id.equals(category.getId()))
//                .findFirst();
//    }
//
//    /** Every product across the entire catalog. */
//    public List<Product> getAllProducts() {
//        List<Product> all = new ArrayList<>();
//        if (rootCategories != null) {
//            for (Category rootCategory : rootCategories) {
//                all.addAll(rootCategory.getAllProducts());
//            }
//        }
//        return all;
//    }

    @Override
    public String toString() {
        return "Catalog{id=" + getId() + ", name='" + name + "', rootCategories="
                + (rootCategories == null ? 0 : rootCategories.size()) + "}";
    }

}
