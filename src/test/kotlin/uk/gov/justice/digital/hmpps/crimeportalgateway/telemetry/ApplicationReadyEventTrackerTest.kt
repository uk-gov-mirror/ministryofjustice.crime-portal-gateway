package uk.gov.justice.digital.hmpps.crimeportalgateway.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties

@ExtendWith(MockitoExtension::class)
internal class ApplicationReadyEventTrackerTest {

    @Mock
    lateinit var telemetryClient: TelemetryClient
    @Mock
    lateinit var buildProperties: BuildProperties
    @Mock
    lateinit var applicationReadyEvent: ApplicationReadyEvent

    @Test
    fun `Emit application ready event with version on startup`() {
        whenever(buildProperties.version).thenReturn("expected-version")
        ApplicationReadyEventTracker(telemetryClient, buildProperties).onApplicationEvent(applicationReadyEvent)

        verify(telemetryClient).trackEvent("ApplicationReady", mapOf<String, String?>("version" to "expected-version"), null)
    }
}
