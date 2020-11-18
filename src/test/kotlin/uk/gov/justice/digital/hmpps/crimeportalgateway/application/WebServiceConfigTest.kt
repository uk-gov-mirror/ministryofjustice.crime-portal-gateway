package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.ws.config.annotation.EnableWs
import org.springframework.ws.config.annotation.WsConfigurerAdapter
import org.springframework.ws.server.EndpointInterceptor
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService

@EnableWs
@TestConfiguration
class WebServiceConfigTest : WsConfigurerAdapter() {

    @MockBean
    private lateinit var telemetryService: TelemetryService

    override fun addInterceptors(interceptors: MutableList<EndpointInterceptor>) {
        interceptors.add(SoapHeaderAddressInterceptor(telemetryService))
    }
}
