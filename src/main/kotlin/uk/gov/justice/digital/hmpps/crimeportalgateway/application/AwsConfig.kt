package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Profile("!test")
@Configuration
class AwsConfig(
    @Value("\${aws.region-name}") private val regionName: String,
    @Value("\${aws.s3.bucket_name}") private val bucketName: String,
    @Value("\${aws.sqs-endpoint-url}") private val sqsEndpointUrl: String
) {

    @Bean
    fun amazonSqs(): AmazonSQSExtendedClient {
        val extendedClientConfig = ExtendedClientConfiguration()
            .withPayloadSupportEnabled(S3Client.builder().region(Region.of(regionName)).build(), bucketName)

        val sqsClient = SqsClient.builder()
            .region(Region.of(regionName))
            .endpointOverride(URI.create(sqsEndpointUrl))
            .build()

        return AmazonSQSExtendedClient(sqsClient, extendedClientConfig)
    }

    @Bean
    fun amazonS3Client(): AmazonS3 {
        return AmazonS3ClientBuilder
            .standard()
            .withRegion(regionName)
            .build()
    }
}
