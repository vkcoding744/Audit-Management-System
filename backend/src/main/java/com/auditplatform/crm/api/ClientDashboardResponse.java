package com.auditplatform.crm.api;

public record ClientDashboardResponse(
        ClientResponse client,
        long siteCount,
        long contactCount,
        long upcomingAudits,
        long completedAudits,
        long openFindings,
        long overdueCapa,
        long activeCertificates,
        long certificatesExpiringSoon,
        long outstandingPayments,
        long documents,
        long openComplaints,
        long openAppeals
) {
}
