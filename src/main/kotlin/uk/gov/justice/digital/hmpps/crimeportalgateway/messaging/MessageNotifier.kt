package uk.gov.justice.digital.hmpps.crimeportalgateway.messaging

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Case
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService

private const val MESSAGE_TYPE = "LIBRA_COURT_CASE"

@Component
class MessageNotifier(
    @Autowired
    private val objectMapper: ObjectMapper,
    @Autowired
    private val telemetryService: TelemetryService,
    @Autowired
    private val amazonSNSClient: AmazonSNS,
    @Value("\${aws.sns.court-case-events-topic}")
    private val topicArn: String
) {
    fun send(case: Case) {
        val message = objectMapper.writeValueAsString(case)
        val subject = "Details for case " + case.caseNo + " in court " + case.courtCode + " published"

        val messageValue = MessageAttributeValue()
            .withDataType("String")
            .withStringValue(MESSAGE_TYPE)

        val publishRequest = PublishRequest(topicArn, message)
            .withMessageAttributes(mapOf("messageType" to messageValue))

        val publishResult = amazonSNSClient.publish(publishRequest)
        log.info("Published message with subject {} with message Id {}", subject, publishResult.messageId)
        telemetryService.trackCourtCaseSplitEvent(case, publishResult.messageId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
