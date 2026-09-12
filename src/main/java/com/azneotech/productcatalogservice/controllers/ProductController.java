package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.CategoryDto;
import com.azneotech.productcatalogservice.dtos.ProductDto;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.services.CategoryService;
import com.azneotech.productcatalogservice.services.ICategoryService;
import com.azneotech.productcatalogservice.services.IProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    private final IProductService productService;
    private final ICategoryService categoryService;

    public  ProductController(IProductService productService, ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDto> responseDtos = products.stream()
                .map(this::mapToProductDto)
                .toList();
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
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
        product.setTitle(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
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
