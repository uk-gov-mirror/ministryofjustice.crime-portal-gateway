package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class AwsConfig(@Value("\${aws.region-name}") private val regionName: String, @Value("\${aws.sqs-endpoint-url}") private val endpointUrl: String) {
    @Bean
    fun amazonSqs(): AmazonSQS {
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(endpointUrl, regionName)

        return AmazonSQSClientBuilder.standard()
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .withEndpointConfiguration(endpointConfiguration)
            .build()
    }
}
