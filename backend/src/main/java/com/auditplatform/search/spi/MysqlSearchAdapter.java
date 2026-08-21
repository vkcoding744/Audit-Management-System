package com.auditplatform.search.spi;

import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.Finding;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.certification.domain.Certificate;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.domain.Lead;
import com.auditplatform.crm.repository.ClientRepository;
import com.auditplatform.crm.repository.LeadRepository;
import com.auditplatform.document.domain.Document;
import com.auditplatform.document.repository.DocumentRepository;
import com.auditplatform.governance.domain.Complaint;
import com.auditplatform.governance.repository.ComplaintRepository;
import com.auditplatform.search.domain.SearchType;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

public class MysqlSearchAdapter implements SearchPort {

    private final ClientRepository clientRepository;
    private final LeadRepository leadRepository;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final CertificateRepository certificateRepository;
    private final DocumentRepository documentRepository;
    private final ComplaintRepository complaintRepository;

    public MysqlSearchAdapter(
            ClientRepository clientRepository,
            LeadRepository leadRepository,
            AuditRepository auditRepository,
            FindingRepository findingRepository,
            CertificateRepository certificateRepository,
            DocumentRepository documentRepository,
            ComplaintRepository complaintRepository
    ) {
        this.clientRepository = clientRepository;
        this.leadRepository = leadRepository;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.certificateRepository = certificateRepository;
        this.documentRepository = documentRepository;
        this.complaintRepository = complaintRepository;
    }

    @Override
    public List<SearchHit> search(String tenantId, String query, SearchType type, int perType) {
        String escaped = LikeQuery.escape(query);
        var page = PageRequest.of(0, Math.max(1, perType));
        List<SearchHit> hits = new ArrayList<>();
        if (include(type, SearchType.CLIENT)) {
            clientRepository.search(tenantId, escaped, page).forEach(client -> hits.add(toHit(client)));
        }
        if (include(type, SearchType.LEAD)) {
            leadRepository.search(tenantId, escaped, page).forEach(lead -> hits.add(toHit(lead)));
        }
        if (include(type, SearchType.AUDIT)) {
            auditRepository.search(tenantId, escaped, page).forEach(audit -> hits.add(toHit(audit)));
        }
        if (include(type, SearchType.FINDING)) {
            findingRepository.search(tenantId, escaped, page).forEach(finding -> hits.add(toHit(finding)));
        }
        if (include(type, SearchType.CERTIFICATE)) {
            certificateRepository.search(tenantId, escaped, page).forEach(certificate -> hits.add(toHit(certificate)));
        }
        if (include(type, SearchType.DOCUMENT)) {
            documentRepository.search(tenantId, escaped, page).forEach(document -> hits.add(toHit(document)));
        }
        if (include(type, SearchType.COMPLAINT)) {
            complaintRepository.search(tenantId, escaped, page).forEach(complaint -> hits.add(toHit(complaint)));
        }
        return hits;
    }

    private static boolean include(SearchType requested, SearchType candidate) {
        return requested == null || requested == candidate;
    }

    private static SearchHit toHit(Client client) {
        return new SearchHit(SearchType.CLIENT, client.getId(), client.getLegalName(), client.getClientNumber(), SearchType.CLIENT.pathFor(client.getId()));
    }

    private static SearchHit toHit(Lead lead) {
        return new SearchHit(SearchType.LEAD, lead.getId(), lead.getOrganisationName(), lead.getLeadNumber(), SearchType.LEAD.pathFor(lead.getId()));
    }

    private static SearchHit toHit(Audit audit) {
        return new SearchHit(SearchType.AUDIT, audit.getId(), audit.getName(), audit.getAuditNumber(), SearchType.AUDIT.pathFor(audit.getId()));
    }

    private static SearchHit toHit(Finding finding) {
        return new SearchHit(SearchType.FINDING, finding.getId(), finding.getTitle(), finding.getFindingNumber(), SearchType.FINDING.pathFor(finding.getId()));
    }

    private static SearchHit toHit(Certificate certificate) {
        return new SearchHit(
                SearchType.CERTIFICATE,
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getStatus() == null ? null : certificate.getStatus().name(),
                SearchType.CERTIFICATE.pathFor(certificate.getId())
        );
    }

    private static SearchHit toHit(Document document) {
        return new SearchHit(SearchType.DOCUMENT, document.getId(), document.getTitle(), document.getDocumentNumber(), SearchType.DOCUMENT.pathFor(document.getId()));
    }

    private static SearchHit toHit(Complaint complaint) {
        return new SearchHit(SearchType.COMPLAINT, complaint.getId(), complaint.getSubject(), complaint.getComplaintNumber(), SearchType.COMPLAINT.pathFor(complaint.getId()));
    }
}
