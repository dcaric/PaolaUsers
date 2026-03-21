package com.paola.paolarestapi.repository;

import com.paola.paolarestapi.persistence.JpaUtil;
import com.paola.paolarestapi.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/*
  UserRepository contains database write logic for UserEntity.

  Where it is used:
  - It is used in UserResource.java (POST /api/users).
  - Flow in UserResource:
      1) request is validated (JSON schema or XSD)
      2) payload is parsed to UserPayload
      3) UserMapper converts UserPayload -> UserEntity
      4) UserRepository.save(userEntity) is called

  How save(...) works:
  - Creates an EntityManager from JpaUtil.
  - Starts a transaction with transaction.begin().
  - Persists the entity with entityManager.persist(...).
  - Commits with transaction.commit() if everything is successful.
  - On runtime error, rolls back with transaction.rollback().
  - Always closes EntityManager in finally block.

  Why we keep this in a repository:
  - REST resource code stays focused on HTTP, validation, and response handling.
  - Persistence concerns (transactions, EntityManager lifecycle) stay in one place.
*/
public class UserRepository {
    public UserEntity save(UserEntity userEntity) {
        EntityManager entityManager = JpaUtil.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(userEntity);
            transaction.commit();
            return userEntity;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
