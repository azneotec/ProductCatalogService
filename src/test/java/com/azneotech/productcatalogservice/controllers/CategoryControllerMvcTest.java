package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.exceptions.AuthException;
import com.azneotech.productcatalogservice.exceptions.AuthExceptionType;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.services.IAuthorizationService;
import com.azneotech.productcatalogservice.services.ICategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
public class CategoryControllerMvcTest {

    private static final String BODY = "{\"name\":\"toys\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICategoryService categoryService;

    @MockBean
    private IAuthorizationService authorizationService;

    @Test
    void createCategory_whenGuardPasses_returns201AndPassesHeaderToGuard() throws Exception {
        Category saved = new Category();
        saved.setId(5L);
        saved.setName("toys");
        when(categoryService.createCategory(any())).thenReturn(saved);

        mockMvc.perform(post("/categories").header("Authorization", "Bearer good")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("toys"));

        verify(authorizationService).requireAdmin("Bearer good");
    }

    @Test
    void createCategory_withoutAuthorizationHeader_passesNullToGuard() throws Exception {
        doThrow(new AuthException("Missing bearer token", AuthExceptionType.UNAUTHENTICATED))
                .when(authorizationService).requireAdmin(null);

        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    void createCategory_whenGuardSaysForbidden_returns403() throws Exception {
        doThrow(new AuthException("Requires ROLE_ADMIN", AuthExceptionType.FORBIDDEN))
                .when(authorizationService).requireAdmin("Bearer user");

        mockMvc.perform(post("/categories").header("Authorization", "Bearer user")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    void createCategory_whenAuthServiceDown_returns503() throws Exception {
        doThrow(new AuthException("down", AuthExceptionType.AUTH_SERVICE_UNAVAILABLE))
                .when(authorizationService).requireAdmin("Bearer any");

        mockMvc.perform(post("/categories").header("Authorization", "Bearer any")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getAllCategories_isNotGuarded() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/categories")).andExpect(status().isOk());

        verify(authorizationService, never()).requireAdmin(any());
    }
}
