package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.amazonaws.services.sqs.model.SendMessageResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SqsService(
    @Value("\${aws_sqs_queue_name:crime-portal-gateway-queue}") private val queueName: String,
    @Autowired private val amazonSqs: AmazonSQS
) {

    fun enqueueMessage(externalDocumentRequest: String): String {
        val msgRequest = SendMessageRequest()
            .withQueueUrl(getQueueUrl())
            .withMessageBody(externalDocumentRequest)
        val value: SendMessageResult = amazonSqs.sendMessage(msgRequest)
        return value.messageId
    }

    fun getQueueUrl(): String {
        return amazonSqs.getQueueUrl(queueName).queueUrl
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
