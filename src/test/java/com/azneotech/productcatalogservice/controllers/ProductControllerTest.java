package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.ProductDto;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.services.IProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProductControllerTest {

    @Autowired
    private ProductController productController;

    @MockBean
    private IProductService productService;

    @Test
    public void testProductDetailsById_WithValidProductId_ReturnProductSuccessfully() {
        // Arrange
        Long productId = 5L;
        Product product = new Product();
        product.setId(productId);
        product.setTitle("Iphone 17");
        when(productService.getProductDetailsById(productId)).thenReturn(product);


        // Act
        ResponseEntity<ProductDto> productDtoResponseEntity =
                productController.getProductDetailsById(productId);


        // Assert
        assertNotNull(productDtoResponseEntity);
        assertNotNull(productDtoResponseEntity.getBody());
        assertEquals(HttpStatus.OK, productDtoResponseEntity.getStatusCode());
        assertEquals(productId, productDtoResponseEntity.getBody().getId());
        assertEquals("Iphone 17", productDtoResponseEntity.getBody().getName());

        verify(productService, times(1))
                .getProductDetailsById(productId);
    }

    @Test
    public void testProductDetailsById_WithNegativeProductId_ResultsInIllegalArgumentException() {
        // Arrange
        Long productId = -1L;

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> productController.getProductDetailsById(productId));

        // Assert
        assertEquals("Please pass id > 0", exception.getMessage());
        verify(productService, times(0)).getProductDetailsById(productId);
    }

}
