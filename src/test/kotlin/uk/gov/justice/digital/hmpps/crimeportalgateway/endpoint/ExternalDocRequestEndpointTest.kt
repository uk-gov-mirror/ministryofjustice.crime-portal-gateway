package uk.gov.justice.digital.hmpps.crimeportalgateway.endpoint

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.magistrates.external.externaldocumentrequest.Documents
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

@ExtendWith(MockitoExtension::class)
internal class ExternalDocRequestEndpointTest {

    @Mock
    private lateinit var telemetryService: TelemetryService

    @Mock
    private lateinit var sqsService: SqsService

    private lateinit var endpoint: ExternalDocRequestEndpoint

    @BeforeEach
    fun beforeEach() {
        endpoint = ExternalDocRequestEndpoint(telemetryService, sqsService, jaxbContext, schema)
    }

    @Test
    fun `given success should the correct acknowledgement message`() {
        val request = ExternalDocumentRequest()
        val documents = Documents()
        request.documents = documents

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest")))
            .thenReturn("a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02")

        val ack = endpoint.processRequest(request)

        verify(telemetryService).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)
        verify(sqsService).enqueueMessage(anyString())
        assertThat(ack).isNotNull
        assertThat(ack.ackType.messageComment).isEqualTo("Message successfully enqueued with id a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02")
        assertThat(ack.ackType.messageStatus).isEqualTo("Success")
        assertThat(ack.ackType.timeStamp).isNotNull
    }

    companion object {

        private lateinit var jaxbContext: JAXBContext

        private lateinit var schema: Schema

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            val resourceLoader: ResourceLoader = DefaultResourceLoader()
            val xsdResource = resourceLoader.getResource("xsd/cp/external/ExternalDocumentRequest.xsd")
            schema = schemaFactory.newSchema(xsdResource.file)
            jaxbContext = JAXBContext.newInstance(ExternalDocumentRequest::class.java)
        }
    }
}
