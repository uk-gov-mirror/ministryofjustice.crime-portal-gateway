package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.ws.config.annotation.WsConfigurerAdapter
import org.springframework.ws.server.EndpointInterceptor
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService

@Configuration
class InterceptorConfig(
    @Autowired private val securityInterceptor: Wss4jSecurityInterceptor?,
    @Autowired private val telemetryService: TelemetryService,
) : WsConfigurerAdapter() {
    override fun addInterceptors(interceptors: MutableList<EndpointInterceptor>) {
        interceptors.add(SoapHeaderAddressInterceptor(telemetryService))
        if (securityInterceptor != null) {
            interceptors.add(securityInterceptor)
        }
    }
}
