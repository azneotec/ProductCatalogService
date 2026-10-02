package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.CategoryDto;
import com.azneotech.productcatalogservice.dtos.PagedResponse;
import com.azneotech.productcatalogservice.dtos.ProductDto;
import com.azneotech.productcatalogservice.dtos.RatingDto;
import com.azneotech.productcatalogservice.mappers.ProductMappers;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.models.Rating;
import com.azneotech.productcatalogservice.services.ICategoryService;
import com.azneotech.productcatalogservice.services.IProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    private final IProductService productService;
    private final ICategoryService categoryService;

    public ProductController(IProductService productService, ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    /**
     * Lists every product, or only those in one category when exactly one of
     * {@code categoryId} / {@code category} (name, case-insensitive) is given.
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category
    ) {
        if (categoryId != null && category != null) {
            throw new IllegalArgumentException("Pass either categoryId or category, not both");
        }
        List<Product> products;
        if (categoryId != null) {
            if (categoryId <= 0L) {
                throw new IllegalArgumentException("Please pass categoryId > 0");
            }
            products = productService.getProductsByCategoryId(categoryId);
        } else if (category != null) {
            if (category.isBlank()) {
                throw new IllegalArgumentException("Please pass a non-blank category name");
            }
            products = productService.getProductsByCategoryName(category);
        } else {
            products = productService.getAllProducts();
        }
        return new ResponseEntity<>(ProductMappers.mapToProductDtos(products), HttpStatus.OK);
    }

    /**
     * Keyword search, best match first. Paged with {@code page} / {@code size}; one or more
     * {@code sort=field[,asc|desc]} params replace relevance order with a Product-property sort.
     */
    @GetMapping("/products/search")
    public ResponseEntity<PagedResponse<ProductDto>> searchProducts(
            @RequestParam(name = "q", required = false) String query,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Please pass a non-blank search query as q");
        }
        Page<ProductDto> results = productService.searchProducts(query, pageable)
                .map(ProductMappers::mapToProductDto);
        return new ResponseEntity<>(PagedResponse.from(results), HttpStatus.OK);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProductDetailsById(@PathVariable Long id) {
        if (id <= 0L) {
            throw new IllegalArgumentException("Please pass id > 0");
        }
        Product product = productService.getProductDetailsById(id);
        if (product == null) {
            throw new RuntimeException("Product is not available");
        }
        return new ResponseEntity<>(ProductMappers.mapToProductDto(product), HttpStatus.OK);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDto> updateProductDetailsById(
            @PathVariable("id") Long productId,
            @RequestBody ProductDto productDto
    ) {
        Product inputProduct = mapToProduct(productDto);
        inputProduct.setId(productId);
        Product updatedProduct = productService.replaceProduct(productId, inputProduct);
        ProductDto responseDto = ProductMappers.mapToProductDto(updatedProduct);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        Product inputProduct = mapToProduct(productDto);
        Product createdProduct = productService.createProduct(inputProduct);
        ProductDto responseDto = ProductMappers.mapToProductDto(createdProduct);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Product mapToProduct(ProductDto productDto) {
        Product product = new Product();
        product.setTitle(productDto.getTitle());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setImageUrl(productDto.getImage());
        if (productDto.getRating() != null) {
            product.setRating(new Rating(productDto.getRating().getRate(), productDto.getRating().getCount()));
        }
        CategoryDto categoryDto = productDto.getCategory();
        if (categoryDto != null && categoryDto.getId() != null) {
            Category category = categoryService.getCategoryById(categoryDto.getId());
            if (category == null) {
                throw new IllegalArgumentException("Category with id " + categoryDto.getId() + " doesn't exist");
            }
            product.setCategory(category);
        }
        return product;
    }

}
