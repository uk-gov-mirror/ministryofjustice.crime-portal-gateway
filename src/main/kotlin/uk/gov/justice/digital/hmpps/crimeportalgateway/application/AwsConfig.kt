package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class AwsConfig(
    @Value("\${aws.region-name}") private val regionName: String,
    @Value("\${aws.sqs-endpoint-url}") private val sqsEndpointUrl: String
) {

    @Bean
    fun amazonSqs(): AmazonSQS {
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(sqsEndpointUrl, regionName)

        return AmazonSQSClientBuilder
            .standard()
            .withEndpointConfiguration(endpointConfiguration)
            .build()
    }

    @Bean
    fun amazonS3Client(): AmazonS3 {
        return AmazonS3ClientBuilder
            .standard()
            .withRegion(regionName)
            .build()
    }
}
