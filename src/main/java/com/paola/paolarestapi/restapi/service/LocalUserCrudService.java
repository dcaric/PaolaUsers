package com.paola.paolarestapi.restapi.service;

import com.paola.paolarestapi.restapi.dto.RestApiUserWriteRequest;
import com.paola.paolarestapi.restapi.model.RestApiUser;
import com.paola.paolarestapi.users.persistence.JpaUtil;
import com.paola.paolarestapi.users.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.List;

/*
  Why it is used:
  - Custom local REST API needs full CRUD over user data.

  How it is used:
  - RestApiResource calls this service for local GET/POST/PUT/DELETE operations.
  - GraphQlService also reuses the same methods for query/mutation flows.

  How it works:
  - Uses JPA EntityManager (from existing Day 1 setup) to read and modify UserEntity records.
  - Maps UserEntity to ReqRes-shaped RestApiUser output model.
*/
public class LocalUserCrudService {

    public List<RestApiUser> findAll() {
        EntityManager entityManager = JpaUtil.createEntityManager();
        try {
            List<UserEntity> entities = entityManager
                    .createQuery("SELECT u FROM UserEntity u ORDER BY u.id", UserEntity.class)
                    .getResultList();
            List<RestApiUser> result = new ArrayList<RestApiUser>();
            for (UserEntity entity : entities) {
                result.add(toModel(entity));
            }
            return result;
        } finally {
            entityManager.close();
        }
    }

    public RestApiUser findById(Long id) {
        EntityManager entityManager = JpaUtil.createEntityManager();
        try {
            UserEntity entity = entityManager.find(UserEntity.class, id);
            return entity == null ? null : toModel(entity);
        } finally {
            entityManager.close();
        }
    }

    public RestApiUser create(RestApiUserWriteRequest request) {
        validateWriteRequest(request);
        EntityManager entityManager = JpaUtil.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            UserEntity entity = new UserEntity();
            copy(request, entity);
            entityManager.persist(entity);
            transaction.commit();
            return toModel(entity);
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public RestApiUser update(Long id, RestApiUserWriteRequest request) {
        validateWriteRequest(request);
        EntityManager entityManager = JpaUtil.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            UserEntity entity = entityManager.find(UserEntity.class, id);
            if (entity == null) {
                transaction.commit();
                return null;
            }
            copy(request, entity);
            entityManager.merge(entity);
            transaction.commit();
            return toModel(entity);
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public boolean delete(Long id) {
        EntityManager entityManager = JpaUtil.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            UserEntity entity = entityManager.find(UserEntity.class, id);
            if (entity == null) {
                transaction.commit();
                return false;
            }
            entityManager.remove(entity);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    private RestApiUser toModel(UserEntity entity) {
        return new RestApiUser(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAvatar()
        );
    }

    private void copy(RestApiUserWriteRequest request, UserEntity entity) {
        entity.setEmail(request.getEmail().trim());
        entity.setFirstName(request.getFirstName().trim());
        entity.setLastName(request.getLastName().trim());
        entity.setAvatar(request.getAvatar().trim());
    }

    private void validateWriteRequest(RestApiUserWriteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("email is required.");
        }
        if (isBlank(request.getFirstName())) {
            throw new IllegalArgumentException("first_name is required.");
        }
        if (isBlank(request.getLastName())) {
            throw new IllegalArgumentException("last_name is required.");
        }
        if (isBlank(request.getAvatar())) {
            throw new IllegalArgumentException("avatar is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
