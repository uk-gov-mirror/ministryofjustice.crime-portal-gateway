package uk.gov.justice.digital.hmpps.crimeportalgateway.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private const val APPLICATION_READY_EVENT = "ApplicationReady"

@Component
class ApplicationReadyEventTracker(
    private val telemetryClient: TelemetryClient,
    private val buildProperties: BuildProperties,
) {
    @EventListener
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        log.info("Posting application ready event to Application Insights")
        val customDimensions = mapOf<String, String?>("version" to buildProperties.version)
        telemetryClient.trackEvent(APPLICATION_READY_EVENT, customDimensions, null)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
