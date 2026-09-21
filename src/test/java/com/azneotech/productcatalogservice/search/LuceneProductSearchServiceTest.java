package com.azneotech.productcatalogservice.search;

import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure Lucene behaviour, no Spring context. */
public class LuceneProductSearchServiceTest {

    private static final Category MENS_CLOTHING = category(1L, "men's clothing");
    private static final Category ACCESSORIES = category(2L, "accessories");

    private static final Product BACKPACK = product(1L,
            "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
            "Your perfect pack for everyday use and walks in the forest.",
            MENS_CLOTHING);
    private static final Product JACKET = product(2L,
            "Mens Cotton Jacket",
            "Great outerwear for spring, autumn and winter.",
            MENS_CLOTHING);
    private static final Product RAIN_COVER = product(3L,
            "Rain Cover",
            "Fits over any jacket to keep you dry.",
            ACCESSORIES);

    private LuceneProductSearchService searchService;

    @BeforeEach
    public void setUp() {
        searchService = new LuceneProductSearchService();
        searchService.reindexAll(List.of(BACKPACK, JACKET, RAIN_COVER));
    }

    @AfterEach
    public void tearDown() {
        searchService.close();
    }

    @Test
    public void testSearch_WithKeywordInTitle_ReturnsMatchingId() {
        assertEquals(List.of(1L), searchService.search("backpack", 10));
    }

    @Test
    public void testSearch_WithKeywordInTitleAndDescription_RanksTitleMatchFirst() {
        List<Long> ids = searchService.search("jacket", 10);

        assertEquals(2, ids.size());
        assertEquals(2L, ids.get(0), "title match must outrank description match");
        assertEquals(3L, ids.get(1));
    }

    @Test
    public void testSearch_WithPrefixOfWord_MatchesFullWord() {
        assertEquals(List.of(1L), searchService.search("back", 10));
    }

    @Test
    public void testSearch_WithPluralAndPossessiveVariants_MatchesViaStemming() {
        // "mens" -> "men" (stem), "men's" -> "men" (possessive) — title + category hit for JACKET,
        // category-only hit for BACKPACK, so JACKET ranks first.
        List<Long> ids = searchService.search("mens", 10);

        assertEquals(List.of(2L, 1L), ids);
    }

    @Test
    public void testSearch_WithCategoryName_MatchesProductsOfThatCategory() {
        List<Long> ids = searchService.search("clothing", 10);

        assertEquals(2, ids.size());
        assertTrue(ids.containsAll(List.of(1L, 2L)));
        assertFalse(ids.contains(3L));
    }

    @Test
    public void testSearch_WithMultipleKeywords_RanksDocumentMatchingMoreOfThemFirst() {
        // "cotton jacket": JACKET matches both, RAIN_COVER only "jacket".
        List<Long> ids = searchService.search("cotton jacket", 10);

        assertEquals(List.of(2L, 3L), ids);
    }

    @Test
    public void testSearch_WithLimit_CapsResults() {
        assertEquals(1, searchService.search("jacket", 1).size());
    }

    @Test
    public void testSearch_WithBlankOrStopWordOnlyQuery_ReturnsEmpty() {
        assertTrue(searchService.search(null, 10).isEmpty());
        assertTrue(searchService.search("", 10).isEmpty());
        assertTrue(searchService.search("   ", 10).isEmpty());
        assertTrue(searchService.search("the and of", 10).isEmpty());
    }

    @Test
    public void testSearch_WithQuerySyntaxCharacters_DoesNotThrow() {
        assertTrue(searchService.search("foo:bar AND (\"", 10).isEmpty());
        assertTrue(searchService.search("*:* OR ~ ^ [", 10).isEmpty());
        // Still finds real words buried in junk. (Note "field:word" stays one token —
        // UAX#29 treats ':' between letters as mid-word — so it's just a non-matching term.)
        assertEquals(List.of(1L), searchService.search("(backpack) AND \"", 10));
        assertTrue(searchService.search("title:backpack", 10).isEmpty());
    }

    @Test
    public void testRemove_ThenSearch_NoLongerFindsProduct() {
        searchService.remove(2L);

        assertEquals(List.of(3L), searchService.search("jacket", 10));
    }

    @Test
    public void testRemove_WithUnknownId_IsNoOp() {
        searchService.remove(999L);

        assertEquals(3, searchService.search("clothing rain", 10).size());
    }

    @Test
    public void testIndex_WithExistingId_ReplacesDocumentWithoutDuplicating() {
        Product renamed = product(1L, "Leather Wallet", "Slim bifold.", ACCESSORIES);

        searchService.index(renamed);

        assertTrue(searchService.search("backpack", 10).isEmpty());
        assertEquals(List.of(1L), searchService.search("wallet", 10));
    }

    @Test
    public void testIndex_WithNewId_AddsDocument() {
        searchService.index(product(4L, "Wool Scarf", null, null));

        assertEquals(List.of(4L), searchService.search("scarf", 10));
    }

    @Test
    public void testReindexAll_ReplacesWholeIndex() {
        searchService.reindexAll(List.of(RAIN_COVER));

        assertTrue(searchService.search("backpack", 10).isEmpty());
        assertEquals(List.of(3L), searchService.search("rain", 10));
    }

    private static Product product(Long id, String title, String description, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setDescription(description);
        product.setCategory(category);
        return product;
    }

    private static Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }
}
