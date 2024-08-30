package uk.gov.justice.digital.hmpps.crimeportalgateway.telemetry

import com.microsoft.applicationinsights.extensibility.ContextInitializer
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ContextInitialiser(
    buildProperties: BuildProperties,
) {
    val version: String = buildProperties.version

    @Bean
    fun versionContextInitializer() = ContextInitializer { it.component.setVersion(version) }
}
