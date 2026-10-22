package com.azneotech.productcatalogservice.services;

public interface IAuthorizationService {

    /**
     * Guards a write endpoint: the {@code Authorization} header must carry a bearer token that
     * UserAuthService reports as valid and that holds {@code ROLE_ADMIN}.
     *
     * @throws com.azneotech.productcatalogservice.exceptions.AuthException {@code UNAUTHENTICATED} for a missing,
     *         malformed or invalid token, {@code FORBIDDEN} without the role, {@code AUTH_SERVICE_UNAVAILABLE}
     *         when UserAuthService can't be reached
     */
    void requireAdmin(String authorizationHeader);

}
