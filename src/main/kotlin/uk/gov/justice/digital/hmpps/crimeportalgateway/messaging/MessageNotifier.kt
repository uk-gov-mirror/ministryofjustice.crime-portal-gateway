package uk.gov.justice.digital.hmpps.crimeportalgateway.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Case
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingTopicException
import uk.gov.justice.hmpps.sqs.publish

private const val MESSAGE_TYPE = "LIBRA_COURT_CASE"

@Component
class MessageNotifier(
    private val objectMapper: ObjectMapper,
    private val telemetryService: TelemetryService,
    private val hmppsQueueService: HmppsQueueService,
) {
    private val topic =
        hmppsQueueService.findByTopicId("courtcaseeventstopic")
            ?: throw MissingTopicException("Could not find topic ")

    fun send(case: Case) {
        val message = objectMapper.writeValueAsString(case)
        val subject = "Details for case " + case.caseNo + " in court " + case.courtCode + " published"

        val messageValue =
            MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(MESSAGE_TYPE).build()

        val publishResult = topic.publish("libra.case.received", message, mapOf("messageType" to messageValue))
        log.info("Published message with subject {} with message Id {}", subject, publishResult.messageId())
        telemetryService.trackCourtCaseSplitEvent(case, publishResult.messageId())
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
