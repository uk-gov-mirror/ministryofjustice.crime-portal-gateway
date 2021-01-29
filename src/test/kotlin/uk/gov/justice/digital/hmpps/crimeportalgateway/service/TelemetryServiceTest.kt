package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.verify as mockitoVerify

@ExtendWith(MockitoExtension::class)
class TelemetryServiceTest {

    @Mock
    private lateinit var telemetryClient: TelemetryClient
    @Mock
    private lateinit var customDimensions: Map<String, String>

    @InjectMocks
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `service sends event name to telemetry`() {

        telemetryService.trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)

        mockitoVerify(telemetryClient).trackEvent("PiCCourtListMessageReceived")
    }
    @Test
    fun `service sends event name and customDimensions to telemetry`() {

        telemetryService.trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED, customDimensions)

        mockitoVerify(telemetryClient).trackEvent("PiCCourtListMessageReceived", customDimensions, null)
    }
}
