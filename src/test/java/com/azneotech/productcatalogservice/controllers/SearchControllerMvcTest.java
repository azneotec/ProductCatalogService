package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.dtos.SortParams;
import com.azneotech.productcatalogservice.dtos.SortType;
import com.azneotech.productcatalogservice.models.Product;
import com.azneotech.productcatalogservice.services.JpaBasedSearchService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
public class SearchControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JpaBasedSearchService searchService;

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<SortParams>> sortParamsCaptor() {
        return ArgumentCaptor.forClass((Class<List<SortParams>>) (Class<?>) List.class);
    }

    @Test
    void testSearchAPI_WithSortParams_PassesThemToService() throws Exception {
        when(searchService.searchProducts(any(), any(), any(), any()))
                .thenReturn(new PageImpl<Product>(List.of()));

        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"shirt","pageSize":5,"pageNumber":0,
                                 "sortParams":[{"sortCriteria":"price","sortType":"DESC"},
                                               {"sortCriteria":"id","sortType":"ASC"}]}"""))
                .andExpect(status().isOk());

        ArgumentCaptor<List<SortParams>> captor = sortParamsCaptor();
        verify(searchService).searchProducts(eq("shirt"), eq(5), eq(0), captor.capture());
        List<SortParams> sortParams = captor.getValue();
        assertEquals(2, sortParams.size());
        assertEquals("price", sortParams.get(0).getSortCriteria());
        assertEquals(SortType.DESC, sortParams.get(0).getSortType());
        assertEquals("id", sortParams.get(1).getSortCriteria());
        assertEquals(SortType.ASC, sortParams.get(1).getSortType());
    }

    @Test
    void testSearchAPI_WithoutSortParams_PassesNullToService() throws Exception {
        when(searchService.searchProducts(any(), any(), any(), any()))
                .thenReturn(new PageImpl<Product>(List.of()));

        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"shirt\",\"pageSize\":5,\"pageNumber\":0}"))
                .andExpect(status().isOk());

        ArgumentCaptor<List<SortParams>> captor = sortParamsCaptor();
        verify(searchService).searchProducts(eq("shirt"), eq(5), eq(0), captor.capture());
        assertNull(captor.getValue());
    }
}
