package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazonaws.services.sqs.AmazonSQS
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.S3Service
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService

@TestConfiguration
class MessagingConfigTest {

    @MockBean
    private lateinit var amazonSQS: AmazonSQS

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @MockBean
    private lateinit var sqsService: SqsService

    @MockBean
    private lateinit var s3Service: S3Service
}
