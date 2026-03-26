package com.paola.paolarestapi.restapi;

import com.paola.paolarestapi.restapi.dto.GraphQlRequest;
import com.paola.paolarestapi.restapi.dto.RestApiUserWriteRequest;
import com.paola.paolarestapi.restapi.model.RestApiUser;
import com.paola.paolarestapi.restapi.service.GraphQlService;
import com.paola.paolarestapi.restapi.service.JwtWriteAccessGuard;
import com.paola.paolarestapi.restapi.service.LocalUserCrudService;
import com.paola.paolarestapi.restapi.service.PublicReqResUserService;
import com.paola.paolarestapi.restapi.service.RestApiSourceSwitchService;
import com.paola.paolarestapi.restapi.service.RestApiSourceType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
  Why it is used:
  - Custom REST API implementation in a dedicated folder/package (`restapi`).
  - Provides ReqRes-like CRUD + GraphQL endpoint + source switch visibility.

  How it is used:
  - Frontend can call /api/restapi/users* for CRUD.
  - Frontend can call /api/restapi/graphql for flexible query/mutation.
  - Write operations require Bearer token with role full-access.
  - GraphQL query is treated as read flow; GraphQL mutation is treated as write flow.

  How it works:
  - GET routes switch between LOCAL DB and PUBLIC ReqRes based on config.
  - POST/PUT/DELETE always write local DB (custom API responsibility).
  - GraphQL endpoint delegates to GraphQlService:
      - users query -> reads local users
      - updateUser mutation -> updates local user by id
*/
@Path("/restapi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RestApiResource {
    private final LocalUserCrudService localUserCrudService = new LocalUserCrudService();
    private final PublicReqResUserService publicReqResUserService = new PublicReqResUserService();
    private final RestApiSourceSwitchService sourceSwitchService = new RestApiSourceSwitchService();
    private final JwtWriteAccessGuard jwtWriteAccessGuard = new JwtWriteAccessGuard();
    private final GraphQlService graphQlService = new GraphQlService();

    @GET
    @Path("/switch")
    public Response getSwitchInfo() {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("activeSource", sourceSwitchService.getActiveSource().name());
        info.put("sourceEnvVar", "REST_API_SOURCE");
        info.put("sourceJvmProperty", "rest.api.source");
        return Response.ok(info).build();
    }

    @GET
    @Path("/users")
    public Response getUsers() {
        if (sourceSwitchService.getActiveSource() == RestApiSourceType.PUBLIC) {
            List<RestApiUser> users = publicReqResUserService.fetchAllUsers();
            return Response.ok(users).build();
        }
        List<RestApiUser> users = localUserCrudService.findAll();
        return Response.ok(users).build();
    }

    @GET
    @Path("/users/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        RestApiUser user;
        if (sourceSwitchService.getActiveSource() == RestApiSourceType.PUBLIC) {
            user = publicReqResUserService.fetchUserById(id);
        } else {
            user = localUserCrudService.findById(id);
        }
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with id " + id + " not found.")
                    .build();
        }
        return Response.ok(user).build();
    }

    @POST
    @Path("/users")
    public Response createUser(
            RestApiUserWriteRequest request,
            @HeaderParam("Authorization") String authorizationHeader
    ) {
        try {
            jwtWriteAccessGuard.ensureWriteAccess(authorizationHeader);
            RestApiUser created = localUserCrudService.create(request);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }
    }

    @PUT
    @Path("/users/{id}")
    public Response updateUser(
            @PathParam("id") Long id,
            RestApiUserWriteRequest request,
            @HeaderParam("Authorization") String authorizationHeader
    ) {
        try {
            jwtWriteAccessGuard.ensureWriteAccess(authorizationHeader);
            RestApiUser updated = localUserCrudService.update(id, request);
            if (updated == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User with id " + id + " not found.")
                        .build();
            }
            return Response.ok(updated).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }
    }

    @DELETE
    @Path("/users/{id}")
    public Response deleteUser(
            @PathParam("id") Long id,
            @HeaderParam("Authorization") String authorizationHeader
    ) {
        jwtWriteAccessGuard.ensureWriteAccess(authorizationHeader);
        boolean deleted = localUserCrudService.delete(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with id " + id + " not found.")
                    .build();
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/graphql")
    public Response graphql(
            GraphQlRequest request,
            @HeaderParam("Authorization") String authorizationHeader
    ) {
        try {
            if (graphQlService.isMutation(request)) {
                jwtWriteAccessGuard.ensureWriteAccess(authorizationHeader);
            }
            Map<String, Object> result = graphQlService.execute(request);
            return Response.ok(result).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }
    }
}
