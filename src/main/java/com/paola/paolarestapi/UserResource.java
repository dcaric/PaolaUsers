package com.paola.paolarestapi;

import com.paola.paolarestapi.users.dto.ErrorResponse;
import com.paola.paolarestapi.users.dto.ValidationViolation;
import com.paola.paolarestapi.users.model.UserPayload;
import com.paola.paolarestapi.users.persistence.UserEntity;
import com.paola.paolarestapi.users.repository.UserRepository;
import com.paola.paolarestapi.users.service.JsonValidationService;
import com.paola.paolarestapi.users.service.UserMapper;
import com.paola.paolarestapi.users.service.XmlValidationService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/users")
public class UserResource {
    private final JsonValidationService jsonValidationService = new JsonValidationService();
    private final XmlValidationService xmlValidationService = new XmlValidationService();
    private final UserRepository userRepository = new UserRepository();
    private final UserMapper userMapper = new UserMapper();

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(String payload, @Context HttpHeaders headers) {
        String contentType = headers.getHeaderString(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                    .entity(new ErrorResponse(
                            "Missing Content-Type header.",
                            java.util.Collections.singletonList(
                                    new ValidationViolation("Content-Type", "required", "Use application/json or application/xml")
                            )
                    ))
                    .build();
        }

        boolean isJson = contentType.toLowerCase().contains(MediaType.APPLICATION_JSON);
        boolean isXml = contentType.toLowerCase().contains(MediaType.APPLICATION_XML);
        if (!isJson && !isXml) {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                    .entity(new ErrorResponse(
                            "Unsupported Content-Type.",
                            java.util.Collections.singletonList(
                                    new ValidationViolation("Content-Type", "unsupported", "Use application/json or application/xml")
                            )
                    ))
                    .build();
        }

        List<ValidationViolation> violations = isJson
                ? jsonValidationService.validate(payload)
                : xmlValidationService.validate(payload);

        if (!violations.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Validation failed.", violations))
                    .build();
        }

        try {
            UserPayload userPayload = isJson
                    ? jsonValidationService.parse(payload)
                    : xmlValidationService.parse(payload);

            UserEntity userEntity = userMapper.toEntity(userPayload);
            UserEntity saved = userRepository.save(userEntity);

            return Response.status(Response.Status.CREATED)
                    .entity(userMapper.toCreatedResponse(saved))
                    .build();
        } catch (Exception exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(
                            "Payload parsing failed.",
                            java.util.Collections.singletonList(
                                    new ValidationViolation("payload", "parse_error", exception.getMessage())
                            )
                    ))
                    .build();
        }
    }
}
