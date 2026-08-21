package com.auditplatform.notification.config;

import com.auditplatform.notification.service.NotificationDispatchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "audit.notifications.dispatch-enabled", havingValue = "true")
public class NotificationDispatchConfiguration {

    private final NotificationDispatchService dispatchService;
    private final int batchSize;

    public NotificationDispatchConfiguration(
            NotificationDispatchService dispatchService,
            @Value("${audit.notifications.dispatch-batch-size:50}") int batchSize
    ) {
        this.dispatchService = dispatchService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${audit.notifications.dispatch-interval-ms:30000}")
    public void dispatchDueJobs() {
        dispatchService.dispatchDueJobs(null, batchSize);
    }
}
