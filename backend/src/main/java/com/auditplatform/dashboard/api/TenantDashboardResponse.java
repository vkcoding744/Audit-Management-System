package com.auditplatform.dashboard.api;

public record TenantDashboardResponse(
        long clients,
        long upcomingAudits,
        long completedAudits,
        long openFindings,
        long overdueCapa,
        long activeCertificates,
        long certificatesExpiringSoon,
        long outstandingInvoices,
        long openComplaints,
        long openAppeals,
        long pendingAiReviews
) {
}
