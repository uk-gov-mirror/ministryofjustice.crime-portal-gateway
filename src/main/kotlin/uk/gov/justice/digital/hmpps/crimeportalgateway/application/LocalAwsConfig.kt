package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Profile(value = ["local", "test"])
@Configuration
class LocalAwsConfig(
    @Value("\${aws.region-name}")
    var regionName: String,
) {
    @Value("\${aws.localstack-endpoint-url}")
    lateinit var endpointUrl: String

    @Bean
    fun amazonS3LocalStackClient(): S3Client {
        return S3Client.builder()
            .endpointOverride(URI.create(endpointUrl))
            .forcePathStyle(true)
            .region(Region.of(regionName))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("any", "any")))
            .build()
    }
}
