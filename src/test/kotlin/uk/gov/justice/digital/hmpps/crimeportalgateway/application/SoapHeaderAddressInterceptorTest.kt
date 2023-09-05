package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.ws.context.MessageContext
import org.springframework.ws.soap.saaj.SaajSoapMessage
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import javax.xml.soap.SOAPElement
import javax.xml.soap.SOAPEnvelope
import javax.xml.soap.SOAPHeader
import javax.xml.soap.SOAPMessage
import javax.xml.soap.SOAPPart

@ExtendWith(MockitoExtension::class)
class SoapHeaderAddressInterceptorTest {

    @Mock
    private lateinit var telemetryService: TelemetryService

    @InjectMocks
    private lateinit var interceptor: SoapHeaderAddressInterceptor

    @Test
    fun `should return true when messageContext is invalid`() {
        val messageContext = mock(MessageContext::class.java)
        val saajSoapResponse = mock(SaajSoapMessage::class.java)
        val saajSoapRequest = mock(SaajSoapMessage::class.java)

        whenever(messageContext.response).thenReturn(saajSoapResponse)
        whenever(messageContext.request).thenReturn(saajSoapRequest)

        assertThat(interceptor.handleResponse(messageContext, null)).isTrue
    }

    @Test
    fun `should return true and update response message with headers`() {
        val messageContext = mock(MessageContext::class.java)
        val responseHeader = mockForHeader(messageContext, true)
        mockForHeader(messageContext, false)

        val actionElement: SOAPElement = mockHeaderChild(responseHeader as SOAPHeader, "Action")
        val messageIdElement: SOAPElement = mockHeaderChild(responseHeader, "MessageID")
        val toElement: SOAPElement = mockHeaderChild(responseHeader, "To")
        val relatesToElement: SOAPElement = mockHeaderChild(responseHeader, "RelatesTo")
        val fromElement: SOAPElement = mock(SOAPElement::class.java)
        `when`(responseHeader.addChildElement("From", "", SoapHeaderAddressInterceptor.SOAP_ENV_ADDRESS_NS))
            .thenReturn(fromElement)
        val addressElement: SOAPElement = mockElementChild(fromElement, "Address")

        assertThat(interceptor.handleResponse(messageContext, null)).isTrue

        verify(actionElement).addTextNode("externalDocument")
        verify(messageIdElement).addTextNode(anyString())
        verify(toElement).addTextNode(anyString())
        verify(relatesToElement).addTextNode(anyString())
        verify(addressElement).addTextNode(anyString())
    }

    @Test
    fun `when fault then telemetry service records`() {
        val messageContext = mock(MessageContext::class.java)
        val saajSoapMessage = mock(SaajSoapMessage::class.java)
        `when`(messageContext.response).thenReturn(saajSoapMessage)

        assertThat(interceptor.handleFault(messageContext, null)).isTrue

        verify(telemetryService).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_ERROR)
    }

    private fun mockForHeader(messageContext: MessageContext, response: Boolean): Any {
        val saajSoapMessage = mock(SaajSoapMessage::class.java)
        val header: SOAPHeader = mock(SOAPHeader::class.java)
        if (response) {
            whenever(messageContext.response).thenReturn(saajSoapMessage)
        } else {
            whenever(messageContext.request).thenReturn(saajSoapMessage)
        }

        mockMessageContext(saajSoapMessage, header)
        return header
    }

    private fun mockHeaderChild(responseHeader: SOAPHeader, elementName: String): SOAPElement {
        val childElement: SOAPElement = mock(SOAPElement::class.java)
        `when`(responseHeader.addChildElement(elementName, "", SoapHeaderAddressInterceptor.SOAP_ENV_ADDRESS_NS))
            .thenReturn(childElement)
        return childElement
    }

    private fun mockElementChild(soapElement: SOAPElement, elementName: String): SOAPElement {
        val childElement: SOAPElement = mock(SOAPElement::class.java)
        `when`(soapElement.addChildElement(elementName, "", SoapHeaderAddressInterceptor.SOAP_ENV_ADDRESS_NS))
            .thenReturn(childElement)
        return childElement
    }

    private fun mockMessageContext(saajSoapMessage: SaajSoapMessage, soapHeader: SOAPHeader) {
        val soapMessage = mock(SOAPMessage::class.java)
        val soapPart = mock(SOAPPart::class.java)
        val soapEnv = mock(SOAPEnvelope::class.java)

        `when`(saajSoapMessage.saajMessage).thenReturn(soapMessage)
        `when`(soapMessage.soapPart).thenReturn(soapPart)
        `when`(soapPart.envelope).thenReturn(soapEnv)
        `when`(soapEnv.header).thenReturn(soapHeader)
    }
}
