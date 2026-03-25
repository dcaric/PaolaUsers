package com.paola.paolarestapi.weather;

import com.paola.paolarestapi.weather.dto.AuthLoginRequest;
import com.paola.paolarestapi.weather.dto.TokenPairResponse;
import com.paola.paolarestapi.weather.dto.TokenRefreshRequest;
import com.paola.paolarestapi.weather.dto.TokenValidationResponse;
import com.paola.paolarestapi.weather.model.WeatherTemperature;
import com.paola.paolarestapi.weather.service.DhmzWeatherService;
import com.paola.paolarestapi.weather.service.JwtTokenService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/*
  REST entry point for Day 3 weather and auth helper endpoints.
  Exposes simple endpoints for temperature search and JWT token flow.
*/
@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class WeatherResource {
    private final DhmzWeatherService dhmzWeatherService = new DhmzWeatherService();
    private final JwtTokenService jwtTokenService = new JwtTokenService();

    @GET
    @Path("/temperature")
    public Response searchWeather(@QueryParam("city") String city) {
        List<WeatherTemperature> results = dhmzWeatherService.findByCity(city);
        return Response.ok(results).build();
    }

    @POST
    @Path("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(AuthLoginRequest request) {
        if (request == null || isBlank(request.getUsername())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new TokenValidationResponse(false, "username is required"))
                    .build();
        }

        String role = normalizeRole(request.getRole());
        TokenPairResponse pair = jwtTokenService.issueTokens(request.getUsername().trim(), role);
        return Response.ok(pair).build();
    }

    @POST
    @Path("/auth/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response refresh(TokenRefreshRequest request) {
        if (request == null || isBlank(request.getRefreshToken())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new TokenValidationResponse(false, "refreshToken is required"))
                    .build();
        }

        TokenPairResponse pair = jwtTokenService.refresh(request.getRefreshToken().trim());
        return Response.ok(pair).build();
    }

    @GET
    @Path("/auth/validate")
    public Response validate(@QueryParam("token") String token) {
        if (isBlank(token)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new TokenValidationResponse(false, "token is required"))
                    .build();
        }
        TokenValidationResponse result = jwtTokenService.validate(token.trim());
        return Response.ok(result).build();
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "read-only";
        }
        String value = role.trim().toLowerCase();
        if ("full-access".equals(value) || "full".equals(value)) {
            return "full-access";
        }
        return "read-only";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
