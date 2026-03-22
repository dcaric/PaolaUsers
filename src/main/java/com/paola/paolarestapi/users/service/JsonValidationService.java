package com.paola.paolarestapi.users.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.paola.paolarestapi.users.dto.ValidationViolation;
import com.paola.paolarestapi.users.model.UserPayload;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
  JsonValidationService handles JSON-side input validation and parsing for POST /api/users.

  What it does:
  - Validates incoming JSON payload against schemas/user.schema.json.
  - Uses JSON Schema validator library to collect schema violations.
  - Parses valid JSON into UserPayload using Jackson.
  - Converts validation/parsing failures into structured ValidationViolation entries.

  How it is used:
  - UserResource calls validate(...) for JSON requests.
  - If no violations are returned, UserResource calls parse(...) to get UserPayload.

  Why we need it:
  - Enforces strict JSON contract before writing to the database.
  - Provides safe JSON parsing after schema checks.
  - Keeps JSON-specific validation/parsing logic out of UserResource.
*/
public class JsonValidationService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final JsonSchema SCHEMA = loadSchema();

    public List<ValidationViolation> validate(String jsonPayload) {
        List<ValidationViolation> violations = new ArrayList<ValidationViolation>();
        try {
            JsonNode node = OBJECT_MAPPER.readTree(jsonPayload);
            Set<ValidationMessage> messages = SCHEMA.validate(node);
            for (ValidationMessage message : messages) {
                violations.add(new ValidationViolation(
                        message.getInstanceLocation().toString(),
                        message.getType(),
                        message.getMessage()
                ));
            }
        } catch (Exception exception) {
            violations.add(new ValidationViolation(
                    "$",
                    "invalid_json",
                    exception.getMessage()
            ));
        }
        return violations;
    }

    public UserPayload parse(String jsonPayload) throws Exception {
        return OBJECT_MAPPER.readValue(jsonPayload, UserPayload.class);
    }

    private static JsonSchema loadSchema() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        InputStream stream = JsonValidationService.class
                .getClassLoader()
                .getResourceAsStream("schemas/user.schema.json");
        if (stream == null) {
            throw new IllegalStateException("Missing JSON schema: schemas/user.schema.json");
        }
        return factory.getSchema(stream);
    }
}

