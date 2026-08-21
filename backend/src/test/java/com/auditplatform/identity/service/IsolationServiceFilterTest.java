package com.auditplatform.identity.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IsolationServiceFilterTest {

    @Test
    void skipsFilterWhenEntityManagerIsNotInATransaction() {
        EntityManager entityManager = mock(EntityManager.class);
        when(entityManager.isOpen()).thenReturn(true);
        when(entityManager.isJoinedToTransaction()).thenReturn(false);
        ObjectProvider<EntityManager> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(entityManager);

        new IsolationService(provider).applyTenantFilter("tenant-a");

        verify(entityManager, never()).unwrap(org.hibernate.Session.class);
    }

    @Test
    void noArgConstructorDoesNotRequireJpa() {
        new IsolationService().applyTenantFilter("tenant-a");
    }
}
