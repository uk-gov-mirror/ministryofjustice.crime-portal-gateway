package uk.gov.justice.digital.hmpps.crimeportalgateway.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.ws.context.MessageContext
import org.springframework.ws.soap.saaj.SaajSoapMessage
import javax.xml.soap.*
import org.mockito.Mockito.`when` as mockitoWhen

@ExtendWith(MockitoExtension::class)
class SoapHeaderAddressInterceptorTest {

    private val interceptor = SoapHeaderAddressInterceptor()

    @Test
    fun `should return true when messageContext is invalid`() {
        val messageContext = mock(MessageContext::class.java)
        val saajSoapResponse = mock(SaajSoapMessage::class.java)
        val saajSoapRequest = mock(SaajSoapMessage::class.java)

        mockitoWhen(messageContext.response).thenReturn(saajSoapResponse)
        mockitoWhen(messageContext.request).thenReturn(saajSoapRequest)

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
        mockitoWhen(responseHeader.addChildElement("From", "", SoapHeaderAddressInterceptor.SOAP_ENV_ADDRESS_NS))
                .thenReturn(fromElement)
        val addressElement: SOAPElement = mockElementChild(fromElement, "Address")

        assertThat(interceptor.handleResponse(messageContext, null)).isTrue

        verify(actionElement).addTextNode("externalDocument")
        verify(messageIdElement).addTextNode(anyString())
        verify(toElement).addTextNode(anyString())
        verify(relatesToElement).addTextNode(anyString())
        verify(addressElement).addTextNode(anyString())
    }

    private fun mockForHeader(messageContext: MessageContext, response: Boolean): Any {
        val saajSoapMessage = mock(SaajSoapMessage::class.java)
        val header: SOAPHeader = mock(SOAPHeader::class.java)
        if (response)
            mockitoWhen(messageContext.response).thenReturn(saajSoapMessage)
        else
            mockitoWhen(messageContext.request).thenReturn(saajSoapMessage)
        mockMessageContext(saajSoapMessage, header)
        return header
    }

    private fun mockHeaderChild(responseHeader: SOAPHeader, elementName: String): SOAPElement {
        val childElement: SOAPElement = mock(SOAPElement::class.java)
        mockitoWhen(responseHeader.addChildElement(elementName, "", SoapHeaderAddressInterceptor.SOAP_ENV_ADDRESS_NS))
                .thenReturn(childElement)
        return childElement
    }

    private fun mockElementChild(soapElement: SOAPElement, elementName: String): SOAPElement {
        val childElement: SOAPElement = mock(SOAPElement::class.java)
        mockitoWhen(soapElement.addChildElement(elementName, "", SoapHeaderAddressInterceptor.SOAP_ENV_ADDRESS_NS))
                .thenReturn(childElement)
        return childElement
    }

    private fun mockMessageContext(saajSoapMessage: SaajSoapMessage, soapHeader: SOAPHeader) {
        val soapMessage = mock(SOAPMessage::class.java)
        val soapPart = mock(SOAPPart::class.java)
        val soapEnv = mock(SOAPEnvelope::class.java)

        mockitoWhen(saajSoapMessage.saajMessage).thenReturn(soapMessage)
        mockitoWhen(soapMessage.soapPart).thenReturn(soapPart)
        mockitoWhen(soapPart.envelope).thenReturn(soapEnv)
        mockitoWhen(soapEnv.header).thenReturn(soapHeader)
    }

}
