package uk.gov.justice.digital.hmpps.crimeportalgateway.endpoint

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Unmarshaller
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageProcessor
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.S3Service
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.MessageDetail
import uk.gov.justice.magistrates.ack.Acknowledgement
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.io.File
import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

@ExtendWith(MockitoExtension::class)
internal class ExternalDocRequestEndpointTest {
    private lateinit var externalDocumentText: String
    private lateinit var externalDocument: ExternalDocumentRequest

    @Mock
    private lateinit var telemetryService: TelemetryService

    @Mock
    private lateinit var s3Service: S3Service

    @Mock
    private lateinit var messageProcessor: MessageProcessor

    private lateinit var endpoint: ExternalDocRequestEndpoint

    private val expectedMessageDetail: MessageDetail =
        MessageDetail(
            hearingDate = "2020-10-26",
            courtCode = "B10JQ",
            courtRoom = 5,
        )

    private val customDimensionsMap =
        mapOf(
            // "sqsMessageId" to "a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02",
            "courtCode" to "B10JQ",
            "courtRoom" to "5",
            "hearingDate" to "2020-10-26",
            "fileName" to "5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY",
        )

    @BeforeEach
    fun beforeEach() {
        endpoint = buildEndpoint(false, 10)
        externalDocumentText = xmlFile.readText().replace(NEWLINE, "")
        externalDocument = marshal(externalDocumentText)
    }

    @Test
    fun `given a valid message then should enqueue the message and return the correct acknowledgement`() {
        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verify(telemetryService).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED, customDimensionsMap)
        verify(messageProcessor).process(anyString())
        verify(s3Service).uploadMessage(eq(expectedMessageDetail), contains("ExternalDocumentRequest"))
        verifyNoMoreInteractions(telemetryService, s3Service)
    }

    @Test
    fun `given a valid message with dummy court room then should not enqueue the message and return the correct acknowledgement`() {
        endpoint = buildEndpoint(false, 5)

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verify(telemetryService).trackEvent(
            TelemetryEventType.COURT_LIST_MESSAGE_IGNORED,
            mapOf(
                "courtCode" to "B10JQ",
                "courtRoom" to "5",
                "fileName" to "5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY",
            ),
        )
        verify(s3Service).uploadMessage(eq(expectedMessageDetail), contains("ExternalDocumentRequest"))
        verifyNoMoreInteractions(telemetryService, messageProcessor, s3Service)
    }

    @Test
    fun `given a message with no usable court code then do not enqueue message`() {
        externalDocument = marshal(xmlFile.readText().replace("5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY", "5_26102020_2992_B10_ADULT_COURT_LIST_DAILY"))

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verify(telemetryService).trackEvent(
            TelemetryEventType.COURT_LIST_MESSAGE_IGNORED,
            mapOf(
                "fileName" to "5_26102020_2992_B10_ADULT_COURT_LIST_DAILY",
            ),
        )
        verify(s3Service).uploadMessage(eq("5_26102020_2992_B10_ADULT_COURT_LIST_DAILY.xml"), contains("ExternalDocumentRequest"))
        verifyNoInteractions(messageProcessor)
    }

    @Test
    fun `given async then success should the correct acknowledgement message`() {
        endpoint = buildEndpoint(true, 50)

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)
        verify(telemetryService, timeout(TIMEOUT_MS)).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED, customDimensionsMap)
        verify(messageProcessor, timeout(TIMEOUT_MS)).process(anyString())
        verify(s3Service, timeout(TIMEOUT_MS)).uploadMessage(eq(expectedMessageDetail), contains("ExternalDocumentRequest"))
        verifyNoMoreInteractions(telemetryService, s3Service)
    }

    private fun assertAck(ack: Acknowledgement) {
        assertThat(ack).isNotNull
        assertThat(ack.ack.messageComment).isEqualTo("MessageComment")
        assertThat(ack.ack.messageStatus).isEqualTo("Success")
        assertThat(ack.ack.timeStamp).isNotNull
    }

    private fun marshal(request: String): ExternalDocumentRequest {
        val marshaller: Unmarshaller = jaxbContext.createUnmarshaller()
        return marshaller.unmarshal(StringReader(request)) as ExternalDocumentRequest
    }

    private fun buildEndpoint(
        aSync: Boolean,
        minDummyCourtRoom: Int,
    ): ExternalDocRequestEndpoint {
        return ExternalDocRequestEndpoint(
            enqueueMsgAsync = aSync,
            xPathForCourtCode = true,
            minDummyCourtRoom = minDummyCourtRoom,
            telemetryService = telemetryService,
            jaxbContext = jaxbContext,
            validationSchema = schema,
            s3Service = s3Service,
            messageProcessor = messageProcessor,
        )
    }

    companion object {
        private const val TIMEOUT_MS: Long = 5000
        private lateinit var xmlFile: File
        private lateinit var jaxbContext: JAXBContext
        private lateinit var schema: Schema
        private val NEWLINE: Regex = Regex("\n")

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            val resourceLoader: ResourceLoader = DefaultResourceLoader()
            val xsdResource = resourceLoader.getResource("xsd/cp/external/ExternalDocumentRequest.xsd")
            schema = schemaFactory.newSchema(xsdResource.file)
            jaxbContext = JAXBContext.newInstance(ExternalDocumentRequest::class.java)
            xmlFile = File("./src/test/resources/external-document-request/request-B10JQ01.xml")
        }
    }
}
