package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.ValidateTokenRequestDto;
import com.azneotech.productcatalogservice.dtos.ValidateTokenResponseDto;
import com.azneotech.productcatalogservice.exceptions.AuthException;
import com.azneotech.productcatalogservice.exceptions.AuthExceptionType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Calls UserAuthService by its Eureka name; the {@code @LoadBalanced} RestTemplate (see RestTemplateConfig)
 * resolves {@code user-auth-service} to one of the registered instances.
 */
@Service
public class AuthServiceClient implements IAuthClient {

    private static final String VALIDATE_TOKEN_URL = "http://user-auth-service/auth/validateToken";

    private final RestTemplate restTemplate;

    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ValidateTokenResponseDto validateToken(String token) {
        try {
            ValidateTokenResponseDto response = restTemplate.postForObject(
                    VALIDATE_TOKEN_URL, new ValidateTokenRequestDto(token), ValidateTokenResponseDto.class);
            if (response == null) {
                throw new AuthException("Empty response from user-auth-service", AuthExceptionType.AUTH_SERVICE_UNAVAILABLE);
            }
            return response;
        } catch (RestClientException | IllegalStateException e) {
            // IllegalStateException is what the load balancer throws when no instance is registered.
            throw new AuthException("Could not reach user-auth-service", AuthExceptionType.AUTH_SERVICE_UNAVAILABLE, e);
        }
    }

}
