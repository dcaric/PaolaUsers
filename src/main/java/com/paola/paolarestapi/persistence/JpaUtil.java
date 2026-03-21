package com.paola.paolarestapi.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/*
  JpaUtil centralizes JPA bootstrap and EntityManager creation.

  1) Where EntityManagerFactory is created
     - It is created here:
         Persistence.createEntityManagerFactory("paolaPU")
     - Yes, it is created at runtime (when this class is initialized by JVM).
     - JPA reads persistence.xml, finds the persistence-unit named "paolaPU",
       and builds a heavyweight factory object.

  2) What EntityManagerFactory is used for
     - It is the main thread-safe factory for creating EntityManager instances.
     - It owns expensive metadata/resources: mappings, provider internals, caches,
       and DB bootstrap configuration.
     - Because it is expensive, we create it once (static final) and reuse it.

  3) What EntityManager is used for
     - EntityManager is the unit-of-work API used by repositories/services.
     - It performs operations like persist/find/merge/remove and manages the
       persistence context for a transaction scope.
     - In this project, UserRepository uses EntityManager.persist(...) to save UserEntity.

  4) Why this utility exists
     - Keeps persistence bootstrapping in one place.
     - Avoids duplicating createEntityManagerFactory(...) across repositories.
     - Makes repository code cleaner: it just asks for createEntityManager().
*/
public final class JpaUtil {
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
            Persistence.createEntityManagerFactory("paolaPU");

    private JpaUtil() {
    }

    public static EntityManager createEntityManager() {
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }
}
