package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Profile(value = ["dev", "preprod", "prod"])
@Configuration
class AwsConfig(
    @Value("\${aws.region-name}")
    var regionName: String,
) {
    @Bean
    fun amazonS3Client(): S3Client {
        return S3Client.builder()
            .region(Region.of(regionName))
            .build()
    }
}
