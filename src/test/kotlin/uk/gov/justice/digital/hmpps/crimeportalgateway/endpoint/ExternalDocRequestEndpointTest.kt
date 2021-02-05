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
        endpoint = buildEndpoint(setOf("B10JQ"), false, 10)
        externalDocument = marshal(xmlFile.readText())
    }

    private val customDimensionsMap = mapOf(
        "sqsMessageId" to "a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02",
        "courtCode" to "B10JQ",
        "courtRoom" to "5",
        "fileName" to "5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY"
    )

    @Test
    fun `given a valid message then should enqueue the message and return the correct acknowledgement`() {

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest")))
            .thenReturn("a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02")

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verify(telemetryService).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED, customDimensionsMap)
        verify(sqsService).enqueueMessage(anyString())
        verifyNoMoreInteractions(sqsService, telemetryService)
    }

    @Test
    fun `given a valid message with dummy court room then should not enqueue the message and return the correct acknowledgement`() {

        endpoint = buildEndpoint(setOf("B10JQ"), false, 5)

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verify(telemetryService).trackEvent(
            TelemetryEventType.COURT_LIST_MESSAGE_IGNORED,
            mapOf(
                "courtCode" to "B10JQ",
                "courtRoom" to "5",
                "fileName" to "5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY"
            )
        )
        verifyNoMoreInteractions(telemetryService, sqsService)
    }

    @Test
    fun `given a message for a court which is not in the include list then should not enqueue message`() {

        endpoint = buildEndpoint(setOf("XXXXX"), false, 5)

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verify(telemetryService).trackEvent(
            TelemetryEventType.COURT_LIST_MESSAGE_IGNORED,
            mapOf(
                "courtCode" to "B10JQ",
                "courtRoom" to "5",
                "fileName" to "5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY"
            )
        )
        verifyZeroInteractions(sqsService)
    }

    @Test
    fun `given a message with no usable court code then do not enqueue message`() {

        externalDocument = marshal(xmlFile.readText().replace("5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY", "5_26102020_2992_B10_ADULT_COURT_LIST_DAILY"))

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)

        verify(telemetryService).trackEvent(
            TelemetryEventType.COURT_LIST_MESSAGE_IGNORED,
            mapOf(
                "fileName" to "5_26102020_2992_B10_ADULT_COURT_LIST_DAILY"
            )
        )
        verifyZeroInteractions(sqsService)
    }

    @Test
    fun `given async then success should the correct acknowledgement message`() {

        endpoint = buildEndpoint(setOf("B10JQ"), true, 50)

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest")))
            .thenReturn("a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02")

        val ack = endpoint.processRequest(externalDocument)

        assertAck(ack)
        verify(telemetryService, timeout(TIMEOUT_MS)).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED, customDimensionsMap)
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

    private fun buildEndpoint(includedCourts: Set<String>, aSync: Boolean, minDummyCourtRoom: Int): ExternalDocRequestEndpoint {
        return ExternalDocRequestEndpoint(
            includedCourts = includedCourts,
            enqueueMsgAsync = aSync,
            xPathForCourtCode = true,
            minDummyCourtRoom = minDummyCourtRoom,
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
            xmlFile = File("./src/test/resources/external-document-request/request-B10JQ01.xml")
        }
    }
}
