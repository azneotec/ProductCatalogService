package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.ProductDto;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.services.ICategoryService;
import com.azneotech.productcatalogservice.services.IProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService productService;

    @MockBean
    private ICategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void TestGetProductDetailsByIdAPI_WithValidId_RunSuccessfully() throws Exception {
        // Arrange
        Product product = new Product();
        product.setId(2L);
        product.setTitle("MacBook Pro");
        when(productService.getProductDetailsById(2L)).thenReturn(product);

        ProductDto productDto = new ProductDto();
        productDto.setId(2L);
        productDto.setName("MacBook Pro");
        String expectedResponse = objectMapper.writeValueAsString(productDto);

        mockMvc.perform(get("/products/2"))  // Act
                .andExpect(status().isOk())           // Assert
                .andExpect(content().string(expectedResponse));
    }

}
