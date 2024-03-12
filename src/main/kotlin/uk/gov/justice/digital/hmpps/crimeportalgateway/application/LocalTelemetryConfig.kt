package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import java.util.UUID

@Profile("local")
class LocalTelemetryConfig : BaseTelemetryConfig() {

    @Bean
    fun getOperationId(): () -> String? {
        return { UUID.randomUUID().toString() }
    }
}
