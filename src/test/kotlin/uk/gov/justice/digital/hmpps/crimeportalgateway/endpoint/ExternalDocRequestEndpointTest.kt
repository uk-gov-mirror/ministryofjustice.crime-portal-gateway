package uk.gov.justice.digital.hmpps.crimeportalgateway.endpoint

import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
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
import uk.gov.justice.magistrates.external.externaldocumentrequest.Acknowledgement
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

    private lateinit var externalDocument: ExternalDocumentRequest

    @Mock
    private lateinit var telemetryService: TelemetryService

    @Mock
    private lateinit var sqsService: SqsService

    private lateinit var endpoint: ExternalDocRequestEndpoint

    @BeforeEach
    fun beforeEach() {
        endpoint = buildEndpoint(setOf("B10JQ"), false)
        externalDocument = marshal(xmlFile.readText())
    }

    @Test
    fun `given a valid message then should enqueue the message and return the correct acknowledgement`() {

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest")))
            .thenReturn("a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02")

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verify(telemetryService).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)
        verify(sqsService).enqueueMessage(anyString())
        verifyNoMoreInteractions(sqsService, telemetryService)
    }

    @Test
    fun `given a message for a court which is not in the include list then should not enqueue message`() {

        endpoint = buildEndpoint(setOf("XXXXX"), false)

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verifyZeroInteractions(telemetryService, sqsService)
    }

    @Test
    fun `given async then success should the correct acknowledgement message`() {

        endpoint = buildEndpoint(setOf("B10JQ"), true)

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest")))
            .thenReturn("a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02")

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)
        verify(telemetryService, timeout(TIMEOUT_MS)).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)
        verify(sqsService, timeout(TIMEOUT_MS)).enqueueMessage(anyString())
        verifyNoMoreInteractions(sqsService, telemetryService)
    }

    private fun assertAck(ack: Acknowledgement) {
        assertThat(ack).isNotNull
        assertThat(ack.ackType.messageComment).isEqualTo("MessageComment")
        assertThat(ack.ackType.messageStatus).isEqualTo("Success")
        assertThat(ack.ackType.timeStamp).isNotNull
    }

    private fun marshal(request: String): ExternalDocumentRequest {
        val marshaller: Unmarshaller = jaxbContext.createUnmarshaller()
        return marshaller.unmarshal(StringReader(request)) as ExternalDocumentRequest
    }

    private fun buildEndpoint(includedCourts: Set<String>, aSync: Boolean): ExternalDocRequestEndpoint {
        return ExternalDocRequestEndpoint(
            includedCourts = includedCourts,
            enqueueMsgAsync = aSync,
            xPathForCourtCode = true,
            telemetryService = telemetryService,
            sqsService = sqsService,
            jaxbContext = jaxbContext,
            validationSchema = schema
        )
    }

    companion object {

        private const val TIMEOUT_MS: Long = 5000
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
