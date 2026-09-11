package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.CategoryDto;
import com.azneotech.productcatalogservice.dtos.ProductDto;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.services.IProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    private final IProductService productService;

    public  ProductController(IProductService productService) {
        this.productService = productService;
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
        return new ResponseEntity<>(mapToProductDto(product), HttpStatus.OK);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDto> updateProductDetailsById(
            @PathVariable("id") Long productId,
            @RequestBody ProductDto productDto
    ) {
        Product inputProduct = mapToProduct(productDto);
        inputProduct.setId(productId);
        Product updatedProduct = productService.replaceProduct(productId, inputProduct);
        ProductDto responseDto = mapToProductDto(updatedProduct);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        Product inputProduct = mapToProduct(productDto);
        Product createdProduct = productService.createProduct(inputProduct);
        ProductDto responseDto = mapToProductDto(createdProduct);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // TODO: GET all products
    // TODO: DELETE product by id
    // TODO: UPDATE product by id

    private ProductDto mapToProductDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getTitle());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());

        if (product.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            categoryDto.setDescription(product.getCategory().getDescription());
            productDto.setCategory(categoryDto);
        }
        return productDto;
    }

    private Product mapToProduct(ProductDto productDto) {
        Product product = new Product();
        product.setId(productDto.getId());
        product.setTitle(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        if (productDto.getCategory() != null) {
            Category category = new Category();
            category.setId(productDto.getCategory().getId());
            category.setName(productDto.getCategory().getName());
            category.setDescription(productDto.getCategory().getDescription());
            product.setCategory(category);
        }
        return product;
    }

}
