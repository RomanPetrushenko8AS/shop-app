package com.example;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static final String PERSISTENCE_UNIT = "shopPU";
    private static final EntityManagerFactory EMF;

    static {
        EMF = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    }

    private JpaUtil() {}

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public static void close() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}