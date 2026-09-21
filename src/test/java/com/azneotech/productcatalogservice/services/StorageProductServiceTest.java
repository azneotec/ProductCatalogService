package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.exceptions.CategoryException;
import com.azneotech.productcatalogservice.exceptions.ProductException;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.repos.CategoryRepository;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import com.azneotech.productcatalogservice.search.IProductSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IProductSearchService searchService;

    @InjectMocks
    private StorageProductService productService;

    @Test
    public void testCreateProduct_SavesWithNullIdThenIndexesSavedEntity() {
        Product input = product(42L, "Client-supplied id");
        Product saved = product(7L, "Client-supplied id");
        when(productRepository.save(input)).thenReturn(saved);

        Product result = productService.createProduct(input);

        assertNull(input.getId(), "id must be cleared so save() inserts rather than merges");
        assertSame(saved, result);
        InOrder inOrder = inOrder(productRepository, searchService);
        inOrder.verify(productRepository).save(input);
        inOrder.verify(searchService).index(saved);
    }

    @Test
    public void testReplaceProduct_WithExistingId_SavesThenIndexes() {
        Product input = product(null, "Updated");
        Product saved = product(3L, "Updated");
        when(productRepository.findById(3L)).thenReturn(Optional.of(product(3L, "Old")));
        when(productRepository.save(input)).thenReturn(saved);

        Product result = productService.replaceProduct(3L, input);

        assertEquals(3L, input.getId());
        assertSame(saved, result);
        verify(searchService).index(saved);
    }

    @Test
    public void testReplaceProduct_WithUnknownId_ThrowsAndDoesNotTouchIndex() {
        when(productRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ProductException.class, () -> productService.replaceProduct(3L, product(null, "x")));

        verify(productRepository, never()).save(any());
        verify(searchService, never()).index(any());
    }

    @Test
    public void testDeleteProduct_WithExistingId_DeletesThenRemovesFromIndex() {
        when(productRepository.existsById(5L)).thenReturn(true);

        productService.deleteProduct(5L);

        InOrder inOrder = inOrder(productRepository, searchService);
        inOrder.verify(productRepository).deleteById(5L);
        inOrder.verify(searchService).remove(5L);
    }

    @Test
    public void testDeleteProduct_WithUnknownId_ThrowsAndDoesNotTouchIndex() {
        when(productRepository.existsById(5L)).thenReturn(false);

        assertThrows(ProductException.class, () -> productService.deleteProduct(5L));

        verify(productRepository, never()).deleteById(anyLong());
        verify(searchService, never()).remove(anyLong());
    }

    @Test
    public void testSearchProducts_PreservesIndexOrderAndDropsIdsMissingFromDatabase() {
        // Index says 3 (best), 2, 1 — but 3 has since been deleted from the DB,
        // and the DB returns rows in id order.
        when(searchService.search("bag", StorageProductService.MAX_SEARCH_RESULTS)).thenReturn(List.of(3L, 2L, 1L));
        Product one = product(1L, "Backpack");
        Product two = product(2L, "Tote bag");
        when(productRepository.findAllById(List.of(3L, 2L, 1L))).thenReturn(List.of(one, two));

        List<Product> results = productService.searchProducts("bag");

        assertEquals(List.of(two, one), results);
    }

    @Test
    public void testSearchProducts_WithNoHits_SkipsDatabase() {
        when(searchService.search("zzz", StorageProductService.MAX_SEARCH_RESULTS)).thenReturn(List.of());

        assertTrue(productService.searchProducts("zzz").isEmpty());

        verify(productRepository, never()).findAllById(anyList());
    }

    @Test
    public void testGetProductsByCategoryId_WithKnownCategory_ReturnsItsProducts() {
        List<Product> expected = List.of(product(1L, "SSD"));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "electronics")));
        when(productRepository.findByCategoryId(1L)).thenReturn(expected);

        assertEquals(expected, productService.getProductsByCategoryId(1L));
    }

    @Test
    public void testGetProductsByCategoryId_WithUnknownCategory_ThrowsCategoryException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryException.class, () -> productService.getProductsByCategoryId(99L));

        verify(productRepository, never()).findByCategoryId(anyLong());
    }

    @Test
    public void testGetProductsByCategoryName_ResolvesCategoryThenQueriesById() {
        List<Product> expected = List.of(product(9L, "Ring"));
        when(categoryRepository.findFirstByNameIgnoreCase("Jewelery")).thenReturn(Optional.of(category(2L, "jewelery")));
        when(productRepository.findByCategoryId(2L)).thenReturn(expected);

        assertEquals(expected, productService.getProductsByCategoryName("Jewelery"));
    }

    @Test
    public void testGetProductsByCategoryName_WithUnknownName_ThrowsCategoryException() {
        when(categoryRepository.findFirstByNameIgnoreCase("nope")).thenReturn(Optional.empty());

        assertThrows(CategoryException.class, () -> productService.getProductsByCategoryName("nope"));
    }

    private static Product product(Long id, String title) {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        return product;
    }

    private static Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }
}
