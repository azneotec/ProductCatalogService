package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.ValidateTokenResponseDto;
import com.azneotech.productcatalogservice.exceptions.AuthException;
import com.azneotech.productcatalogservice.exceptions.AuthExceptionType;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceClientTest {

    private static final String URL = "http://user-auth-service/auth/validateToken";

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final AuthServiceClient client = new AuthServiceClient(restTemplate);

    @Test
    void validateToken_postsToServiceNameAndReturnsBody() {
        ValidateTokenResponseDto body = ValidateTokenResponseDto.builder().valid(true).roles(List.of("ROLE_ADMIN")).build();
        when(restTemplate.postForObject(eq(URL), any(), eq(ValidateTokenResponseDto.class))).thenReturn(body);

        ValidateTokenResponseDto result = client.validateToken("t");

        assertTrue(result.isValid());
        assertEquals(List.of("ROLE_ADMIN"), result.getRoles());
    }

    @Test
    void validateToken_whenServiceUnreachable_throwsUnavailable() {
        when(restTemplate.postForObject(eq(URL), any(), eq(ValidateTokenResponseDto.class)))
                .thenThrow(new ResourceAccessException("refused"));

        AuthException e = assertThrows(AuthException.class, () -> client.validateToken("t"));
        assertEquals(AuthExceptionType.AUTH_SERVICE_UNAVAILABLE, e.getExceptionType());
    }

    @Test
    void validateToken_whenNoInstanceRegistered_throwsUnavailable() {
        when(restTemplate.postForObject(eq(URL), any(), eq(ValidateTokenResponseDto.class)))
                .thenThrow(new IllegalStateException("No instances available for user-auth-service"));

        AuthException e = assertThrows(AuthException.class, () -> client.validateToken("t"));
        assertEquals(AuthExceptionType.AUTH_SERVICE_UNAVAILABLE, e.getExceptionType());
    }
}
