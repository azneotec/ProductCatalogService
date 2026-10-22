package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.ValidateTokenResponseDto;
import com.azneotech.productcatalogservice.exceptions.AuthException;
import com.azneotech.productcatalogservice.exceptions.AuthExceptionType;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements IAuthorizationService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final IAuthClient authClient;

    public AuthorizationService(IAuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public void requireAdmin(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())
                || authorizationHeader.substring(BEARER_PREFIX.length()).isBlank()) {
            throw new AuthException("Missing bearer token", AuthExceptionType.UNAUTHENTICATED);
        }
        ValidateTokenResponseDto user = authClient.validateToken(authorizationHeader.substring(BEARER_PREFIX.length()).trim());
        if (!user.isValid()) {
            throw new AuthException("Invalid or expired token", AuthExceptionType.UNAUTHENTICATED);
        }
        if (!user.getRoles().contains(ADMIN_ROLE)) {
            throw new AuthException("Requires " + ADMIN_ROLE, AuthExceptionType.FORBIDDEN);
        }
    }

}
