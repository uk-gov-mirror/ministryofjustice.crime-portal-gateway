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

    @InjectMocks
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `service send event name to telemetry`() {

        telemetryService.trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)

        mockitoVerify(telemetryClient).trackEvent("PiCCourtListMessageReceived")
    }
}
