package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.ValidateTokenResponseDto;
import com.azneotech.productcatalogservice.exceptions.AuthException;
import com.azneotech.productcatalogservice.exceptions.AuthExceptionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    private final IAuthClient authClient = mock(IAuthClient.class);
    private final AuthorizationService service = new AuthorizationService(authClient);

    private static ValidateTokenResponseDto valid(String... roles) {
        return ValidateTokenResponseDto.builder().valid(true).userId(1L).email("a@b.c").roles(List.of(roles)).build();
    }

    private AuthExceptionType failureOf(String header) {
        return assertThrows(AuthException.class, () -> service.requireAdmin(header)).getExceptionType();
    }

    @Test
    void admin_passes() {
        when(authClient.validateToken("good")).thenReturn(valid("ROLE_USER", "ROLE_ADMIN"));

        assertDoesNotThrow(() -> service.requireAdmin("Bearer good"));
    }

    @Test
    void schemeIsCaseInsensitive() {
        when(authClient.validateToken("good")).thenReturn(valid("ROLE_ADMIN"));

        assertDoesNotThrow(() -> service.requireAdmin("bearer good"));
    }

    @Test
    void missingOrMalformedHeader_isUnauthenticatedWithoutCallingAuthService() {
        assertEquals(AuthExceptionType.UNAUTHENTICATED, failureOf(null));
        assertEquals(AuthExceptionType.UNAUTHENTICATED, failureOf("Basic abc"));
        assertEquals(AuthExceptionType.UNAUTHENTICATED, failureOf("Bearer "));
        assertEquals(AuthExceptionType.UNAUTHENTICATED, failureOf("Bearer"));

        verify(authClient, never()).validateToken(any());
    }

    @Test
    void invalidToken_isUnauthenticated() {
        when(authClient.validateToken("bad")).thenReturn(ValidateTokenResponseDto.builder().valid(false).build());

        assertEquals(AuthExceptionType.UNAUTHENTICATED, failureOf("Bearer bad"));
    }

    @Test
    void validTokenWithoutAdminRole_isForbidden() {
        when(authClient.validateToken("user")).thenReturn(valid("ROLE_USER"));

        assertEquals(AuthExceptionType.FORBIDDEN, failureOf("Bearer user"));
    }

    @Test
    void authServiceFailure_propagates() {
        when(authClient.validateToken("any")).thenThrow(
                new AuthException("down", AuthExceptionType.AUTH_SERVICE_UNAVAILABLE));

        assertEquals(AuthExceptionType.AUTH_SERVICE_UNAVAILABLE, failureOf("Bearer any"));
    }
}
