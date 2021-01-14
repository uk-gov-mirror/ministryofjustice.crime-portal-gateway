package uk.gov.justice.digital.hmpps.crimeportalgateway.endpoint

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.DocumentUtils
import uk.gov.justice.magistrates.external.externaldocumentrequest.AckType
import uk.gov.justice.magistrates.external.externaldocumentrequest.Acknowledgement
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.io.StringWriter
import java.time.LocalDateTime
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.validation.Schema

@Endpoint
class ExternalDocRequestEndpoint(
    @Value("#{'\${included-court-codes}'.split(',')}") private val includedCourts: Set<String>,
    @Autowired val telemetryService: TelemetryService,
    @Autowired val sqsService: SqsService,
    @Autowired val jaxbContext: JAXBContext,
    @Autowired val validationSchema: Schema?
) {

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = REQUEST_LOCAL_NAME)
    @ResponsePayload
    fun processRequest(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        log.info("Request payload received. {}", request.documents?.toString())

        val courtCode = request.documents?.any?.let { DocumentUtils.getCourtCode(it) }
        val message: String

        when (includedCourts.contains(courtCode)) {
            true -> {
                val messageId = sqsService.enqueueMessage(marshal(request))
                message = String.format(SUCCESS_MESSAGE_COMMENT, messageId)
                telemetryService.trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)
                log.info("Message enqueued with ID {} ", messageId)
            }
            false -> {
                message = courtCode?.let { String.format(IGNORED_MESSAGE_UNKNOWN_COURT, it) } ?: IGNORED_MESSAGE_NO_COURT
            }
        }

        return Acknowledgement().apply {
            ackType = AckType().apply {
                messageComment = message
                messageStatus = SUCCESS_MESSAGE_STATUS
                timeStamp = LocalDateTime.now()
            }
        }
    }

    private fun marshal(request: ExternalDocumentRequest): String {
        val marshaller: Marshaller = jaxbContext.createMarshaller()
        validationSchema?.let { marshaller.schema = it }
        val sw = StringWriter()
        marshaller.marshal(request, sw)
        return sw.toString()
    }

    companion object {
        const val SUCCESS_MESSAGE_STATUS = "Success"
        const val SUCCESS_MESSAGE_COMMENT = "Message successfully enqueued with id %s"
        const val IGNORED_MESSAGE_UNKNOWN_COURT = "Message ignored - the court %s in the message is not processed"
        const val IGNORED_MESSAGE_NO_COURT = "Message ignored - no court code found in the message"
        const val NAMESPACE_URI = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
        const val REQUEST_LOCAL_NAME = "ExternalDocumentRequest"
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
