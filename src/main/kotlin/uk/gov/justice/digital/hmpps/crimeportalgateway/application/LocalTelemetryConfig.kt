package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.UUID

@Profile(value = ["local", "test"])
@Configuration
class LocalTelemetryConfig : BaseTelemetryConfig() {

    @Bean
    @Conditional(AppInsightKeyAbsentCondition::class)
    fun getTelemetryClient(): TelemetryClient {
        return TelemetryClient()
    }

    @Bean
    fun getOperationId(): () -> String? {
        return { UUID.randomUUID().toString() }
    }
}
