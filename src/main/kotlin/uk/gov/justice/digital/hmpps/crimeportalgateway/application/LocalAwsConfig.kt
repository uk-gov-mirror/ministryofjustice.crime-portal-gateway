package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile(value = ["local", "test"])
@Configuration
class LocalAwsConfig(
    @Value("\${aws.region-name}")
    var regionName: String
) {

    @Value("\${aws.localstack-endpoint-url}")
    lateinit var endpointUrl: String

    @Bean
    fun amazonS3LocalStackClient(): AmazonS3 {
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(endpointUrl, regionName)

        return AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(endpointConfiguration)
            .withPathStyleAccessEnabled(true)
            .withCredentials(StaticCredentialsProvider(BasicAWSCredentials("any", "any")))
            .build()
    }

    @Bean
    fun amazonSNSLocalStackClient(): AmazonSNS {
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(endpointUrl, regionName)

        return AmazonSNSClientBuilder
            .standard()
            .withCredentials(StaticCredentialsProvider(BasicAWSCredentials("any", "any")))
            .withEndpointConfiguration(endpointConfiguration)
            .build()
    }

    @Bean
    fun amazonSQSLocalStackClient(): AmazonSQS {
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(endpointUrl, regionName)

        return AmazonSQSClientBuilder
            .standard()
            .withCredentials(StaticCredentialsProvider(BasicAWSCredentials("any", "any")))
            .withEndpointConfiguration(endpointConfiguration)
            .build()
    }
}
