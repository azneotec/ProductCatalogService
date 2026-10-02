package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.exceptions.CategoryException;
import com.azneotech.productcatalogservice.exceptions.CategoryExceptionType;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.models.Rating;
import com.azneotech.productcatalogservice.services.ICategoryService;
import com.azneotech.productcatalogservice.services.IProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService productService;

    @MockBean
    private ICategoryService categoryService;

    private static Product product(Long id, String title, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setPrice(109.95f);
        product.setDescription("A description");
        product.setImageUrl("https://example.com/" + id + ".png");
        product.setCategory(category);
        product.setRating(new Rating(3.9, 120));
        return product;
    }

    private static Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    @Test
    public void testGetProductDetailsByIdAPI_WithValidId_ReturnsFakeStoreShape() throws Exception {
        // Arrange
        Product product = product(2L, "MacBook Pro", category(1L, "electronics"));
        when(productService.getProductDetailsById(2L)).thenReturn(product);

        // Act + Assert
        mockMvc.perform(get("/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("MacBook Pro"))
                .andExpect(jsonPath("$.price").value(109.95))
                .andExpect(jsonPath("$.description").value("A description"))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("electronics"))
                .andExpect(jsonPath("$.category.description").doesNotExist())
                .andExpect(jsonPath("$.image").value("https://example.com/2.png"))
                .andExpect(jsonPath("$.rating.rate").value(3.9))
                .andExpect(jsonPath("$.rating.count").value(120));
    }

    @Test
    public void testGetProductDetailsByIdAPI_WithNonPositiveId_Returns400() throws Exception {
        mockMvc.perform(get("/products/0"))
                .andExpect(status().isBadRequest());
        verify(productService, never()).getProductDetailsById(anyLong());
    }

    @Test
    public void testGetProductsAPI_WithoutFilters_ReturnsAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(
                product(1L, "Backpack", null),
                product(2L, "Jacket", null)));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Backpack"))
                .andExpect(jsonPath("$[1].title").value("Jacket"))
                .andExpect(jsonPath("$[0].category").doesNotExist());
    }

    @Test
    public void testGetProductsAPI_WithCategoryId_ReturnsProductsOfThatCategory() throws Exception {
        Category electronics = category(1L, "electronics");
        when(productService.getProductsByCategoryId(1L)).thenReturn(List.of(product(7L, "SSD", electronics)));

        mockMvc.perform(get("/products").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].category.name").value("electronics"));
        verify(productService, never()).getAllProducts();
    }

    @Test
    public void testGetProductsAPI_WithCategoryName_ReturnsProductsOfThatCategory() throws Exception {
        Category jewelery = category(2L, "jewelery");
        when(productService.getProductsByCategoryName("jewelery")).thenReturn(List.of(product(9L, "Ring", jewelery)));

        mockMvc.perform(get("/products").param("category", "jewelery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Ring"));
    }

    @Test
    public void testGetProductsAPI_WithUnknownCategoryId_Returns404() throws Exception {
        when(productService.getProductsByCategoryId(99L)).thenThrow(
                new CategoryException("Category with id 99 doesn't exist", CategoryExceptionType.CATEGORY_NOT_FOUND));

        mockMvc.perform(get("/products").param("categoryId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProductsAPI_WithNonNumericCategoryId_Returns400() throws Exception {
        mockMvc.perform(get("/products").param("categoryId", "abc"))
                .andExpect(status().isBadRequest());
        verify(productService, never()).getProductsByCategoryId(anyLong());
    }

    @Test
    public void testGetProductsAPI_WithNonPositiveCategoryId_Returns400() throws Exception {
        mockMvc.perform(get("/products").param("categoryId", "-1"))
                .andExpect(status().isBadRequest());
        verify(productService, never()).getProductsByCategoryId(anyLong());
    }

    @Test
    public void testGetProductsAPI_WithBothFilters_Returns400() throws Exception {
        mockMvc.perform(get("/products").param("categoryId", "1").param("category", "electronics"))
                .andExpect(status().isBadRequest());
        verify(productService, never()).getProductsByCategoryId(anyLong());
        verify(productService, never()).getProductsByCategoryName(anyString());
    }

    @Test
    public void testSearchProductsAPI_WithQuery_ReturnsPagedMatchesInServiceOrder() throws Exception {
        when(productService.searchProducts(eq("bag"), any(Pageable.class))).thenAnswer(inv ->
                new PageImpl<>(List.of(product(3L, "Tote Bag", null), product(1L, "Backpack", null)),
                        inv.<Pageable>getArgument(1), 25));

        mockMvc.perform(get("/products/search").param("q", "bag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[1].id").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    public void testSearchProductsAPI_WithoutPagingParams_UsesDefaultUnsortedFirstPage() throws Exception {
        when(productService.searchProducts(eq("bag"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/products/search").param("q", "bag")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).searchProducts(eq("bag"), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
        assertTrue(captor.getValue().getSort().isUnsorted());
    }

    @Test
    public void testSearchProductsAPI_WithPageSizeAndSort_PassesPageableToService() throws Exception {
        when(productService.searchProducts(eq("bag"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/products/search")
                        .param("q", "bag").param("page", "1").param("size", "2")
                        .param("sort", "price,desc").param("sort", "id,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).searchProducts(eq("bag"), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
        assertEquals(List.of(Sort.Order.desc("price"), Sort.Order.asc("id")), pageable.getSort().toList());
    }

    @Test
    public void testSearchProductsAPI_WithOversizedPage_ClampsToMaxPageSize() throws Exception {
        when(productService.searchProducts(eq("bag"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/products/search").param("q", "bag").param("size", "500"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).searchProducts(eq("bag"), captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    public void testSearchProductsAPI_WithBlankQuery_Returns400() throws Exception {
        mockMvc.perform(get("/products/search").param("q", "   "))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/products/search"))
                .andExpect(status().isBadRequest());
        verify(productService, never()).searchProducts(anyString(), any(Pageable.class));
    }

}
