package com.auditplatform.search.config;

import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.crm.repository.ClientRepository;
import com.auditplatform.crm.repository.LeadRepository;
import com.auditplatform.document.repository.DocumentRepository;
import com.auditplatform.governance.repository.ComplaintRepository;
import com.auditplatform.search.spi.MysqlSearchAdapter;
import com.auditplatform.search.spi.SearchPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class SearchConfiguration {

    @Bean
    @ConditionalOnProperty(name = "audit.search.provider", havingValue = "mysql", matchIfMissing = true)
    public SearchPort mysqlSearchAdapter(
            ClientRepository clientRepository,
            LeadRepository leadRepository,
            AuditRepository auditRepository,
            FindingRepository findingRepository,
            CertificateRepository certificateRepository,
            DocumentRepository documentRepository,
            ComplaintRepository complaintRepository
    ) {
        return new MysqlSearchAdapter(
                clientRepository,
                leadRepository,
                auditRepository,
                findingRepository,
                certificateRepository,
                documentRepository,
                complaintRepository
        );
    }
}
