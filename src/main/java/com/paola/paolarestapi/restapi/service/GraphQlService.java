package com.paola.paolarestapi.restapi.service;

import com.paola.paolarestapi.restapi.dto.GraphQlRequest;
import com.paola.paolarestapi.restapi.dto.RestApiUserWriteRequest;
import com.paola.paolarestapi.restapi.model.RestApiUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
  What GraphQL is:
  - GraphQL lets clients request only the fields they want and execute mutations through a single endpoint.
  - It is useful when frontend needs flexible data selection without adding many custom REST routes.

  Why it is used:
  - Custom REST API requires a GraphQL endpoint over the same user data.
  - It allows query + mutation behavior while keeping DB model shared with REST CRUD.

  How it is used:
  - RestApiResource forwards POST /api/restapi/graphql requests to this service.
  - Supports two assignment-focused operations:
    - users query
    - updateUser mutation

  How it works:
  - Reads GraphQlRequest.query text and performs lightweight operation routing.
  - If query contains "users", it returns local users list.
  - If query contains "updateUser", it reads variables and updates one local user.
  - Returns GraphQL-style response shape: { "data": { ... } }.

  Scope note:
  - This is a minimal implementation for project requirements, not a full GraphQL parser/engine.
*/
public class GraphQlService {
    private final LocalUserCrudService localUserCrudService = new LocalUserCrudService();

    public Map<String, Object> execute(GraphQlRequest request) {
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("GraphQL query is required.");
        }

        String query = request.getQuery().toLowerCase(Locale.ROOT);
        if (query.contains("updateuser")) {
            return executeUpdateUser(request);
        }
        if (query.contains("users")) {
            return executeUsersQuery();
        }
        throw new IllegalArgumentException("Unsupported GraphQL operation. Supported: users, updateUser.");
    }

    public boolean isMutation(GraphQlRequest request) {
        return request != null
                && request.getQuery() != null
                && request.getQuery().toLowerCase(Locale.ROOT).contains("mutation");
    }

    private Map<String, Object> executeUsersQuery() {
        List<RestApiUser> users = localUserCrudService.findAll();
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("users", users);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("data", data);
        return response;
    }

    private Map<String, Object> executeUpdateUser(GraphQlRequest request) {
        Map<String, Object> variables = request.getVariables();
        if (variables == null) {
            throw new IllegalArgumentException("GraphQL variables are required for updateUser mutation.");
        }

        Long id = asLong(variables.get("id"));
        if (id == null) {
            throw new IllegalArgumentException("GraphQL variable 'id' is required for updateUser.");
        }

        RestApiUserWriteRequest writeRequest = new RestApiUserWriteRequest();
        writeRequest.setEmail(asString(variables.get("email")));
        writeRequest.setFirstName(asString(variables.get("first_name")));
        writeRequest.setLastName(asString(variables.get("last_name")));
        writeRequest.setAvatar(asString(variables.get("avatar")));

        RestApiUser updated = localUserCrudService.update(id, writeRequest);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("updateUser", updated);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("data", data);
        return response;
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
