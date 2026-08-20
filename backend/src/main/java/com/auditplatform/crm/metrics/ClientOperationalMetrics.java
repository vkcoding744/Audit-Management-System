package com.auditplatform.crm.metrics;

public record ClientOperationalMetrics(
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
    public static ClientOperationalMetrics empty() {
        return new ClientOperationalMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
