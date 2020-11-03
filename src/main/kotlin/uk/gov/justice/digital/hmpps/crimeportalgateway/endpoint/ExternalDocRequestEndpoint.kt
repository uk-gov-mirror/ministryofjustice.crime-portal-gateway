package uk.gov.justice.digital.hmpps.crimeportalgateway.endpoint

import org.slf4j.LoggerFactory
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload
import uk.gov.justice.magistrates.external.externaldocumentrequest.AckType
import uk.gov.justice.magistrates.external.externaldocumentrequest.Acknowledgement
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.time.LocalDateTime

@Endpoint
class ExternalDocRequestEndpoint {

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = REQUEST_LOCAL_NAME)
    @ResponsePayload
    fun processRequest(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        log.info("Request payload received. Document count {}", request.documents?.document?.size ?: 0)
        val ackType = AckType()
        ackType.messageComment = SUCCESS_MESSAGE_COMMENT
        ackType.messageStatus = SUCCESS_MESSAGE_STATUS
        ackType.timeStamp = LocalDateTime.now()
        val acknowledgement = Acknowledgement()
        acknowledgement.ackType = ackType
        return acknowledgement
    }

    companion object {
        const val SUCCESS_MESSAGE_STATUS = "MessageStatus"
        const val SUCCESS_MESSAGE_COMMENT = "MessageComment"
        const val NAMESPACE_URI = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
        const val REQUEST_LOCAL_NAME = "ExternalDocumentRequest"
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
