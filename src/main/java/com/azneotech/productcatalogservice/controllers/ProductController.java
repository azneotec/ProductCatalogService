package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.CategoryDto;
import com.azneotech.productcatalogservice.dtos.ProductDto;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.services.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    private final IProductService productService;

    public  ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products/{id}")
    public ProductDto getProductDetailsById(@PathVariable Long id) {
        Product product = productService.getProductDetailsById(id);

        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getTitle());
        productDto.setDescription(product.getDescription());

        if (product.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            productDto.setCategory(categoryDto);
        }

        return productDto;
    }

    @GetMapping("/products/{pid}/{cid}")
    private ProductDto getProductDetailsById(
            @PathVariable("pid") Long productId,
            @PathVariable("cid") Long categoryId
    ) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(categoryId);
        categoryDto.setName("Category Name");
        categoryDto.setDescription("Category Description");

        ProductDto productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setName("Product Name");
        productDto.setDescription("Product Description");
        productDto.setCategory(categoryDto);

        return productDto;
    }

}
