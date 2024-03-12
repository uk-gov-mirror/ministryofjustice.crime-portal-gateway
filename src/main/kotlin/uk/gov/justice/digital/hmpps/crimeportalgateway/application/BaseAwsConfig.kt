package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile(value = ["!test", "!local"])
@Configuration
class BaseAwsConfig {
    @Value("\${aws.region-name}")
    lateinit var regionName: String
}
