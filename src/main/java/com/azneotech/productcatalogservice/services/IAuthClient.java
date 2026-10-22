package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.ValidateTokenResponseDto;

public interface IAuthClient {

    /**
     * Asks UserAuthService whether {@code token} is a live session token.
     *
     * @throws com.azneotech.productcatalogservice.exceptions.AuthException {@code AUTH_SERVICE_UNAVAILABLE} when
     *                                                                       UserAuthService can't be reached
     */
    ValidateTokenResponseDto validateToken(String token);

}
