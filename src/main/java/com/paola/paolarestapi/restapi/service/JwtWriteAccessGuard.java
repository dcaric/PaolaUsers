package com.paola.paolarestapi.restapi.service;

import com.paola.paolarestapi.weather.dto.TokenValidationResponse;
import com.paola.paolarestapi.weather.service.JwtTokenService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/*
  Why it is used:
  - Custom REST API requires role-based authorization: read-only can GET, full-access can write.

  How it is used:
  - RestApiResource calls ensureWriteAccess(...) before POST, PUT, DELETE, and GraphQL mutations.

  How it works:
  - Reads Bearer token from Authorization header.
  - Validates token via JwtTokenService.
  - Allows only role=full-access; otherwise throws 401/403 response.
*/
public class JwtWriteAccessGuard {
    private final JwtTokenService jwtTokenService = new JwtTokenService();

    public void ensureWriteAccess(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new WebApplicationException(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Missing or invalid Authorization header. Use Bearer <token>.")
                            .build()
            );
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        TokenValidationResponse validation = jwtTokenService.validate(token);
        if (!validation.isValid()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Invalid token: " + validation.getMessage())
                            .build()
            );
        }

        String role = validation.getRole();
        if (!"full-access".equals(role)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity("Role '" + role + "' cannot perform write operations.")
                            .build()
            );
        }
    }
}
