package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import software.amazon.awssdk.services.sqs.model.SqsException

@Component
class SqsService(
    @Value("\${aws_sqs_queue_name:crime-portal-gateway-queue}") private val queueName: String,
    @Autowired private val sqsClient: AmazonSQSExtendedClient,

    @Autowired
    @Qualifier("getOperationId")
    private val getOperationId: () -> String
) {

    fun enqueueMessage(externalDocumentRequest: String): String? {
        log.debug("Entered enqueueMessage()")
        val sendMessageRequest = SendMessageRequest.builder()
            .queueUrl(getQueueUrl())
            .messageBody(externalDocumentRequest)
            .messageAttributes(
                mapOf(
                    "operation_Id" to MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(getOperationId())
                        .build()
                )
            )
            .build()

        val value: SendMessageResponse? = sqsClient.sendMessage(sendMessageRequest)
        return value?.messageId()
    }

    fun getQueueUrl(): String {
        val queueUrl =
            try {
                sqsClient.getQueueUrl(
                    GetQueueUrlRequest.builder()
                        .queueName(queueName).build()
                ).queueUrl()
            } catch (e: SqsException) {
                val errorMessage = "Could not get queue : $queueName. ${e.awsErrorDetails().errorMessage()}"
                log.error(errorMessage, e)
                throw IllegalStateException(errorMessage)
            }
        log.debug("Queue URL is : $queueUrl")
        return queueUrl
    }

    fun isQueueAvailable(): Boolean {
        try {
            return getQueueUrl().isNotEmpty()
        } catch (exception: Exception) {
            log.error("Unable to find the required queue for {}", queueName, exception)
        }
        return false
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
