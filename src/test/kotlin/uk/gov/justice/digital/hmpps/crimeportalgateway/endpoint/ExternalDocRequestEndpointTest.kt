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
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.io.File
import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
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
        endpoint = ExternalDocRequestEndpoint(setOf("B10JQ"), telemetryService, sqsService, jaxbContext, schema)
    }

    @Test
    fun `given success should the correct acknowledgement message`() {

        val externalDocument = marshal(xmlFile.readText())

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest")))
            .thenReturn("a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02")

        val ack = endpoint.processRequest(externalDocument)

        verify(telemetryService).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)
        verify(sqsService).enqueueMessage(anyString())
        assertThat(ack).isNotNull
        assertThat(ack.ackType.messageComment).isEqualTo("Message successfully enqueued with id a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02")
        assertThat(ack.ackType.messageStatus).isEqualTo("Success")
        assertThat(ack.ackType.timeStamp).isNotNull
    }

    private fun marshal(request: String): ExternalDocumentRequest {
        val marshaller: Unmarshaller = jaxbContext.createUnmarshaller()
        return marshaller.unmarshal(StringReader(request)) as ExternalDocumentRequest
    }

    companion object {

        private lateinit var xmlFile: File
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
            xmlFile = File("./src/test/resources/external-document-request/request-B10JQ.xml")
        }
    }
}
