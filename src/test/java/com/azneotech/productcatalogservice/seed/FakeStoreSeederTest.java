package com.azneotech.productcatalogservice.seed;

import com.azneotech.productcatalogservice.dtos.FakeStoreProductDto;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.repos.CategoryRepository;
import com.azneotech.productcatalogservice.repos.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FakeStoreSeederTest {

    @Mock
    private FakeStoreClient fakeStoreClient;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FakeStoreSeeder seeder;

    @Captor
    private ArgumentCaptor<List<Product>> productsCaptor;

    @Test
    public void testRun_WhenProductsAlreadyExist_DoesNothing() {
        when(productRepository.count()).thenReturn(5L);

        seeder.run(new DefaultApplicationArguments());

        verifyNoInteractions(fakeStoreClient);
        verify(productRepository, never()).saveAll(anyList());
        verifyNoInteractions(categoryRepository);
    }

    @Test
    public void testRun_WhenTableEmpty_MapsProductsAndFindsOrCreatesCategoriesOnce() {
        Category electronics = category(1L, "electronics");
        Category toys = category(5L, "toys");
        when(productRepository.count()).thenReturn(0L);
        when(fakeStoreClient.fetchAllProducts()).thenReturn(List.of(
                dto(1L, "SSD", 79.99f, "Fast storage", "electronics", "https://img/1.png", 4.5, 300),
                dto(2L, "Teddy", 12.0f, "Soft", "toys", "https://img/2.png", null, null),
                dto(3L, "Blocks", 20.0f, "Stackable", "toys", "https://img/3.png", 4.0, 10)));
        when(categoryRepository.findFirstByNameIgnoreCase("electronics")).thenReturn(Optional.of(electronics));
        when(categoryRepository.findFirstByNameIgnoreCase("toys")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(toys);

        seeder.run(new DefaultApplicationArguments());

        verify(productRepository).saveAll(productsCaptor.capture());
        List<Product> saved = productsCaptor.getValue();
        assertEquals(3, saved.size());

        Product ssd = saved.get(0);
        assertNull(ssd.getId(), "FakeStore ids must not be forced onto IDENTITY columns");
        assertEquals("SSD", ssd.getTitle());
        assertEquals("Fast storage", ssd.getDescription());
        assertEquals(79.99f, ssd.getPrice());
        assertEquals("https://img/1.png", ssd.getImageUrl());
        assertEquals(4.5, ssd.getRating().getRate());
        assertEquals(300, ssd.getRating().getCount());
        assertSame(electronics, ssd.getCategory());

        Product teddy = saved.get(1);
        assertNull(teddy.getRating());
        assertSame(toys, teddy.getCategory());
        assertSame(toys, saved.get(2).getCategory());

        // "toys" appears twice but is looked up and created exactly once.
        verify(categoryRepository, times(1)).findFirstByNameIgnoreCase("toys");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void testRun_WhenFakeStoreUnreachable_SkipsWithoutFailing() {
        when(productRepository.count()).thenReturn(0L);
        when(fakeStoreClient.fetchAllProducts()).thenThrow(new ResourceAccessException("connect timed out"));

        assertDoesNotThrow(() -> seeder.run(new DefaultApplicationArguments()));

        verify(productRepository, never()).saveAll(anyList());
        verifyNoInteractions(categoryRepository);
    }

    private static FakeStoreProductDto dto(Long id, String title, Float price, String description,
                                           String category, String image, Double rate, Integer count) {
        FakeStoreProductDto dto = new FakeStoreProductDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setPrice(price);
        dto.setDescription(description);
        dto.setCategory(category);
        dto.setImage(image);
        if (rate != null) {
            FakeStoreProductDto.Rating rating = new FakeStoreProductDto.Rating();
            rating.setRate(rate);
            rating.setCount(count);
            dto.setRating(rating);
        }
        return dto;
    }

    private static Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }
}
