package dev.neate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Scheduled service to retry failed Spring Modulith events.
 * 
 * This component periodically checks for incomplete event publications
 * in the EventPublicationRegistry and resubmits them for processing.
 * 
 * Configuration:
 * - Enabled via spring.modulith.events.retry-scheduled=true
 * - Runs every 5 minutes by default
 * - Retries events older than 1 minute
 */
@Component
@ConditionalOnProperty(name = "spring.modulith.events.retry-scheduled", havingValue = "true", matchIfMissing = false)
public class ScheduledEventRetryService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledEventRetryService.class);

    private final IncompleteEventPublications incompleteEventPublications;

    public ScheduledEventRetryService(IncompleteEventPublications incompleteEventPublications) {
        this.incompleteEventPublications = incompleteEventPublications;
    }

    /**
     * Scheduled task to retry incomplete event publications.
     * 
     * Runs every 5 minutes and retries events that have been incomplete
     * for more than 1 minute to avoid immediate retry loops.
     */
    @Scheduled(fixedRateString = "${spring.modulith.events.retry-interval:300000}") // 5 minutes default
    public void retryIncompleteEvents() {
        log.debug("Starting scheduled retry of incomplete events");
        
        try {
            // Retry events that have been incomplete for more than 1 minute
            incompleteEventPublications.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(1));
            log.debug("Completed scheduled retry of incomplete events");
            
        } catch (Exception e) {
            log.error("Error during scheduled event retry", e);
        }
    }
}
