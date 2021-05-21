package uk.gov.justice.digital.hmpps.crimeportalgateway.endpoint

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.S3Service
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.DocumentUtils
import uk.gov.justice.magistrates.ack.AckType
import uk.gov.justice.magistrates.ack.Acknowledgement
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import javax.annotation.PostConstruct
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.validation.Schema

private const val SQS_MESSAGE_ID_LABEL = "sqsMessageId"
private const val COURT_CODE_LABEL = "courtCode"
private const val COURT_ROOM_LABEL = "courtRoom"
private const val HEARING_DATE_LABEL = "hearingDate"
private const val FILENAME_LABEL = "fileName"

@Endpoint
class ExternalDocRequestEndpoint(
    @Value("#{'\${included-court-codes}'.split(',')}") private val includedCourts: Set<String>,
    @Value("\${enqueue-msg-async:true}") private val enqueueMsgAsync: Boolean,
    @Value("\${use-xpath-for-court-code:true}") private val xPathForCourtCode: Boolean,
    @Value("\${min-dummy-court-room:50}") private val minDummyCourtRoom: Int,
    @Autowired private val telemetryService: TelemetryService,
    @Autowired private val sqsService: SqsService,
    @Autowired private val s3Service: S3Service,
    @Autowired private val jaxbContext: JAXBContext,
    @Autowired private val validationSchema: Schema?
) {

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = REQUEST_LOCAL_NAME)
    @ResponsePayload
    fun processPayloadRootRequest(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        log.debug("Request payload received to PayloadRoot mapped. {}", request.documents?.toString())

        return process(request)
    }

    @SoapAction("externalDocument")
    @ResponsePayload
    fun processRequestExternalDocument(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        log.debug("Request payload received to externalDocument SoapAction. {}", request.documents?.toString())

        return process(request)
    }

    @SoapAction("")
    @ResponsePayload
    fun processRequest(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        log.debug("Request payload received to ExternalDocument SoapAction. {}", request.documents?.toString())

        return process(request)
    }

    private fun process(request: ExternalDocumentRequest): Acknowledgement {

        when (enqueueMsgAsync) {
            true -> {
                CompletableFuture
                    .supplyAsync<Any> { enqueueMessage(request) }
                    .exceptionally { exception: Throwable? ->
                        log.error("Error from enqueuing message", exception)
                    }
            }
            false -> {
                enqueueMessage(request)
            }
        }

        return Acknowledgement().apply {
            ack = AckType().apply {
                messageComment = "MessageComment"
                messageStatus = SUCCESS_MESSAGE_STATUS
                timeStamp = LocalDateTime.now()
            }
        }
    }

    fun enqueueMessage(request: ExternalDocumentRequest) {
        val fileName = request.documents?.any?.let { DocumentUtils.getFileName(it) }

        val messageDetail = request.documents?.any?.let { DocumentUtils.getMessageDetail(it, xPathForCourtCode) }
            ?: kotlin.run {
                log.info(IGNORED_MESSAGE_NO_COURT)
                telemetryService.trackEvent(
                    TelemetryEventType.COURT_LIST_MESSAGE_IGNORED,
                    mapOf(
                        FILENAME_LABEL to fileName,
                    )
                )
                val failedFileName = fileName ?: "fail-" + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())
                s3Service.uploadMessage("$failedFileName.xml", marshal(request, validate = false))
                return
            }

        val messageContent = marshal(request)
        when (includedCourts.contains(messageDetail.courtCode) && messageDetail.courtRoom < minDummyCourtRoom) {
            true -> {
                val sqsMessageId = sqsService.enqueueMessage(messageContent)
                telemetryService.trackEvent(
                    TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED,
                    mapOf(
                        SQS_MESSAGE_ID_LABEL to sqsMessageId,
                        COURT_CODE_LABEL to messageDetail.courtCode,
                        COURT_ROOM_LABEL to messageDetail.courtRoom.toString(),
                        HEARING_DATE_LABEL to messageDetail.hearingDate,
                        FILENAME_LABEL to fileName
                    )
                )
                log.info(String.format(SUCCESS_MESSAGE_COMMENT, messageDetail.courtCode, messageDetail.courtRoom, sqsMessageId))
            }
            false -> {
                log.info(String.format(IGNORED_MESSAGE_UNKNOWN_COURT, messageDetail.courtCode, messageDetail.courtRoom))
                telemetryService.trackEvent(
                    TelemetryEventType.COURT_LIST_MESSAGE_IGNORED,
                    mapOf(
                        COURT_CODE_LABEL to messageDetail.courtCode,
                        COURT_ROOM_LABEL to messageDetail.courtRoom.toString(),
                        FILENAME_LABEL to fileName
                    )
                )
            }
        }

        s3Service.uploadMessage(messageDetail, messageContent)
    }

    private fun marshal(request: ExternalDocumentRequest, validate: Boolean = true): String {
        val marshaller: Marshaller = jaxbContext.createMarshaller()
        if (validate) {
            validationSchema?.let { marshaller.schema = it }
        }
        val sw = StringWriter()
        marshaller.marshal(request, sw)
        val msg = sw.toString()
        log.trace(msg)
        return msg
    }

    @PostConstruct
    private fun initialise() {
        log.info("Included courts {}", includedCourts)
    }

    companion object {
        const val SUCCESS_MESSAGE_STATUS = "Success"
        const val SUCCESS_MESSAGE_COMMENT = "Message successfully enqueued for court %s / room %s with id %s"
        const val IGNORED_MESSAGE_UNKNOWN_COURT = "Message ignored - the court %s / room %s values in the message is not processed"
        const val IGNORED_MESSAGE_NO_COURT = "Message ignored - no court code found in the message"
        const val NAMESPACE_URI = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
        const val REQUEST_LOCAL_NAME = "ExternalDocumentRequest"
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
