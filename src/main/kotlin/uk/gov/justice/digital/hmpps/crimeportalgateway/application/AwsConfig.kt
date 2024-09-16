package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile(value = ["dev", "preprod", "prod"])
@Configuration
class AwsConfig(
    @Value("\${aws.region-name}")
    var regionName: String,
) {
    @Bean
    fun amazonS3Client(): AmazonS3 {
        return AmazonS3ClientBuilder
            .standard()
            .withRegion(regionName)
            .build()
    }
}
