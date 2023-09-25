package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse

@ExtendWith(MockitoExtension::class)
internal class SqsServiceTest {

    @Mock
    private lateinit var sqsClient: AmazonSQSExtendedClient
    private lateinit var sqsService: SqsService

    private val operationId = { "operation-123" }
    private val queueName = "crime-portal-gateway-queue"
    private val queueUrl = "http://localhost:4566/000000000000/crime-portal-gateway-queue"
    private lateinit var getQueueUrlRequest: GetQueueUrlRequest

    @BeforeEach
    fun beforeEach() {
        sqsService = SqsService(queueName, sqsClient, operationId)
        getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build()

        whenever(sqsClient.getQueueUrl(getQueueUrlRequest)).thenReturn(
            GetQueueUrlResponse
                .builder()
                .queueUrl(queueUrl)
                .build()
        )
    }

    @Test
    fun `should give queue as available`() {
        assertThat(sqsService.isQueueAvailable()).isTrue
    }

    @Test
    fun `should not give queue as available when not`() {
        whenever(sqsClient.getQueueUrl(getQueueUrlRequest))
            .thenReturn(GetQueueUrlResponse.builder().build())

        assertThat(sqsService.isQueueAvailable()).isFalse
    }

    @Test
    fun `should enqueue a message`() {
        // Given
        val msgRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody("Hello World")
            .messageAttributes(
                mapOf(
                    "operation_Id" to software.amazon.awssdk.services.sqs.model.MessageAttributeValue.builder()
                        .stringValue("operation-123")
                        .dataType("String")
                        .build()
                )
            ).build()

        whenever(sqsClient.sendMessage(msgRequest)).thenReturn(
            SendMessageResponse.builder().messageId("ID").build()
        )

        // When
        val id = sqsService.enqueueMessage("Hello World")

        // Then
        assertThat(id).isEqualTo("ID")
    }
}
