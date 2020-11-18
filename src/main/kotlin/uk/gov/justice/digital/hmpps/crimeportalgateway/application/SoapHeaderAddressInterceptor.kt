package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.slf4j.LoggerFactory
import org.springframework.ws.context.MessageContext
import org.springframework.ws.server.EndpointInterceptor
import org.springframework.ws.soap.saaj.SaajSoapMessage
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.HashMap
import java.util.UUID
import javax.xml.soap.SOAPElement
import javax.xml.soap.SOAPException
import javax.xml.soap.SOAPHeader
import javax.xml.soap.SOAPHeaderElement

class SoapHeaderAddressInterceptor(private val telemetryService: TelemetryService) : EndpointInterceptor {

    override fun handleRequest(p0: MessageContext?, p1: Any?): Boolean {
        return true
    }

    override fun handleResponse(messageContext: MessageContext, p1: Any?): Boolean {
        val soapResponseMessage = messageContext.response as SaajSoapMessage
        val soapRequestMessage = messageContext.request as SaajSoapMessage

        val soapResponseHeader = soapResponseMessage.saajMessage?.soapPart?.envelope?.header
        if (soapResponseHeader != null) {
            val requestHeaders: Map<String, String> =
                getHeaderMap(soapRequestMessage.saajMessage?.soapPart?.envelope?.header)
            addTextNodeToNewElement(
                soapResponseHeader,
                "Action",
                requestHeaders["Action"] ?: "externalDocument"
            )
            addTextNodeToNewElement(soapResponseHeader, "MessageID", UUID.randomUUID().toString())
            addTextNodeToNewElement(
                soapResponseHeader,
                "To",
                requestHeaders["From"] ?: ""
            )
            addTextNodeToNewElement(
                soapResponseHeader,
                "RelatesTo",
                requestHeaders["MessageID"] ?: ""
            )

            val soapElement = soapResponseHeader.addChildElement("From", "", SOAP_ENV_ADDRESS_NS)
            addTextNodeToNewElement(soapElement, "Address", requestHeaders["To"] ?: "")
        }

        return true
    }

    override fun handleFault(messageContext: MessageContext, p1: Any?): Boolean {
        telemetryService.trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_ERROR)
        val buffer = ByteArrayOutputStream()
        messageContext.response.writeTo(buffer)
        log.error(buffer.toString(StandardCharsets.UTF_8.name()))
        return true
    }

    override fun afterCompletion(messageContext: MessageContext, p1: Any, p2: Exception?) {
//        TODO("Not yet implemented")
    }

    private fun addTextNodeToNewElement(soapElement: SOAPElement, elementName: String, elementTextNode: String) {
        try {
            val childElement: SOAPElement = soapElement.addChildElement(elementName, "", SOAP_ENV_ADDRESS_NS)
            childElement.addTextNode(elementTextNode)
        } catch (e: SOAPException) {
            log.error("Unable to create child node with text for {}", elementName, e)
        }
    }

    private fun getHeaderMap(soapHeader: SOAPHeader?): Map<String, String> {
        val headerMap: MutableMap<String, String> = HashMap()

        val headerElements: MutableIterator<SOAPHeaderElement>? =
            if (soapHeader != null) soapHeader.examineAllHeaderElements()
            else Collections.emptyIterator()

        headerElements?.forEachRemaining { hdr: SOAPHeaderElement ->
            headerMap[hdr.localName] = hdr.textContent.trim()
        }
        return headerMap
    }

    companion object {
        const val SOAP_ENV_ADDRESS_NS = "http://www.w3.org/2005/08/addressing"
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
