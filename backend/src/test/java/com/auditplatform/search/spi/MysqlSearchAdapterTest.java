package com.auditplatform.search.spi;

import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.repository.ClientRepository;
import com.auditplatform.crm.repository.LeadRepository;
import com.auditplatform.document.repository.DocumentRepository;
import com.auditplatform.governance.repository.ComplaintRepository;
import com.auditplatform.search.domain.SearchType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MysqlSearchAdapterTest {

    @Test
    void searchesOnlyRequestedTenantAndEscapesWildcards() {
        ClientRepository clients = mock(ClientRepository.class);
        Client client = new Client();
        ReflectionTestUtils.setField(client, "id", "c1");
        client.setLegalName("Acme Ltd");
        client.setClientNumber("CLIENT-000001");
        when(clients.search(eq("tenant-a"), eq("Acme\\%"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(client)));

        MysqlSearchAdapter adapter = new MysqlSearchAdapter(
                clients,
                mock(LeadRepository.class),
                mock(AuditRepository.class),
                mock(FindingRepository.class),
                mock(CertificateRepository.class),
                mock(DocumentRepository.class),
                mock(ComplaintRepository.class)
        );

        List<SearchHit> hits = adapter.search("tenant-a", "Acme%", SearchType.CLIENT, 5);

        verify(clients).search(eq("tenant-a"), eq("Acme\\%"), any(Pageable.class));
        assertThat(hits).hasSize(1);
        assertThat(hits.getFirst().path()).isEqualTo("/clients/c1");
        assertThat(hits.getFirst().title()).isEqualTo("Acme Ltd");
    }
}
