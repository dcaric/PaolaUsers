package com.paola.paolarestapi.service;

import com.paola.paolarestapi.dto.UserCreatedResponse;
import com.paola.paolarestapi.model.UserPayload;
import com.paola.paolarestapi.persistence.UserEntity;

/*
  UserMapper centralizes transformation rules between API and persistence models.

  What it does:
  - toEntity(...): maps UserPayload (API request model) -> UserEntity (DB model).
  - toCreatedResponse(...): maps UserEntity -> UserCreatedResponse (API success DTO).

  How it is used:
  - UserResource calls toEntity(...) before UserRepository.save(...).
  - UserResource calls toCreatedResponse(...) to build the 201 response body.

  Why we need it:
  - Keeps mapping logic out of UserResource so resource code stays focused on
    HTTP flow, validation decisions, and response status handling.
  - Makes mapping rules easy to maintain in one place when models evolve.
*/
public class UserMapper {
    public UserEntity toEntity(UserPayload payload) {
        UserEntity entity = new UserEntity();
        entity.setEmail(payload.getEmail());
        entity.setFirstName(payload.getFirstName());
        entity.setLastName(payload.getLastName());
        entity.setAvatar(payload.getAvatar());
        return entity;
    }

    public UserCreatedResponse toCreatedResponse(UserEntity entity) {
        UserCreatedResponse response = new UserCreatedResponse();
        response.setId(entity.getId());
        response.setEmail(entity.getEmail());
        response.setFirst_name(entity.getFirstName());
        response.setLast_name(entity.getLastName());
        response.setAvatar(entity.getAvatar());
        return response;
    }
}
