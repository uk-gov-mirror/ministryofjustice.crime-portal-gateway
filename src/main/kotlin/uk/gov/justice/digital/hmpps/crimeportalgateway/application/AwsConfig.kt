package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
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
    @Value("\${aws.sqs-endpoint-url}") private val sqsEndpointUrl: String,
    @Value("\${aws.s3.access_key_id}") private val s3AccessKeyId: String,
    @Value("\${aws.s3.secret_access_key}") private val s3SecretAccessKey: String
) {

    @Bean
    fun amazonSqs(): AmazonSQS {
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(sqsEndpointUrl, regionName)

        return AmazonSQSClientBuilder.standard()
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .withEndpointConfiguration(endpointConfiguration)
            .build()
    }

    @Bean
    fun amazonS3Client(): AmazonS3 {
        val credentials: AWSCredentials = BasicAWSCredentials(s3AccessKeyId, s3SecretAccessKey)

        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(regionName)
            .build()
    }
}
