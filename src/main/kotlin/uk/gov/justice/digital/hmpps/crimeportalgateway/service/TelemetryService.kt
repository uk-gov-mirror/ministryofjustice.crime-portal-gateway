package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TelemetryService (@Autowired private val telemetryClient: TelemetryClient) {

    fun trackEvent(eventType: TelemetryEventType) {
        telemetryClient.trackEvent(eventType.eventName)
    }
}
