package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.startsWith
import org.mockito.Mockito.contains
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.ws.test.server.MockWebServiceClient
import org.springframework.ws.test.server.RequestCreators
import org.springframework.ws.test.server.ResponseMatchers
import org.springframework.ws.test.server.ResponseMatchers.noFault
import org.springframework.ws.test.server.ResponseMatchers.validPayload
import org.springframework.ws.test.server.ResponseMatchers.xpath
import org.springframework.xml.transform.StringSource
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.MessagingConfigTest
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.S3Service
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.MessageDetail
import java.io.File
import javax.xml.transform.Source

@Import(MessagingConfigTest::class)
class ExternalDocRequestEndpointIntTest : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var sqsService: SqsService

    @Autowired
    private lateinit var s3Service: S3Service

    private lateinit var mockClient: MockWebServiceClient

    @BeforeEach
    fun before() {
        mockClient = MockWebServiceClient.createClient(applicationContext)
    }

    @Test
    fun `should send message to SQS queue with a size greater than SQS Limit`() {
        // Given
        val externalDoc = readFile("src/test/resources/soap/sample-request-large.xml")
        val requestEnvelope: Source = StringSource(externalDoc)

        // When
        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment")
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success")
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        // Then
        verify(sqsService).enqueueMessage(anyString())
        val expectedMessageDetail = MessageDetail(courtCode = "B10JQ", courtRoom = 1, hearingDate = "2020-02-27")
        verify(s3Service).uploadMessage(eq(expectedMessageDetail), contains("ExternalDocumentRequest"))
        verifyNoMoreInteractions(sqsService, s3Service)
    }

    @Test
    fun `should enqueue message and return successful acknowledgement`() {
        val externalDoc = readFile("src/test/resources/soap/sample-request.xml")
        val requestEnvelope: Source = StringSource(externalDoc)

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest"))).thenReturn(sqsMessageId)

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment")
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success")
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        verify(telemetryService).trackEvent(
            TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED,
            mapOf(
                "sqsMessageId" to "a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02",
                "courtCode" to "B10JQ",
                "courtRoom" to "0",
                "hearingDate" to "2020-10-26",
                "fileName" to "5_26102020_2992_B10JQ00_ADULT_COURT_LIST_DAILY"
            )
        )
        verify(sqsService).enqueueMessage(anyString())
        val expectedMessageDetail = MessageDetail(courtCode = "B10JQ", courtRoom = 0, hearingDate = "2020-10-26")
        verify(s3Service).uploadMessage(eq(expectedMessageDetail), contains("ExternalDocumentRequest"))
        verifyNoMoreInteractions(sqsService, s3Service)
    }

    @Test
    fun `should not enqueue message when court is not processed but return acknowledgement`() {
        val externalDoc1 = readFile("src/test/resources/soap/ignored-courts.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment")
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success")
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        val expectedMessageDetail = MessageDetail(courtCode = "B10XX", courtRoom = 0, hearingDate = "2020-10-26")
        verify(s3Service).uploadMessage(eq(expectedMessageDetail), contains("ExternalDocumentRequest"))
        verifyNoMoreInteractions(s3Service)
        verifyNoInteractions(sqsService)
    }

    @Test
    fun `given no court present`() {
        val requestEnvelope: Source = StringSource(readFile("src/test/resources/soap/sample-request-invalid-xml.xml"))

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment")
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success")
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        verify(s3Service).uploadMessage(startsWith("fail"), contains("ExternalDocumentRequest"))
        verifyNoMoreInteractions(s3Service)
        verifyNoInteractions(sqsService)
    }

    @Test
    fun `given no SQS available then SOAP Fault`() {
        val externalDoc1 = readFile("src/test/resources/soap/sample-request.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest")))
            .thenThrow(IllegalArgumentException())

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(ResponseMatchers.serverOrReceiverFault())
            .andExpect(xpath("//env:Fault/env:Code/env:Value", namespaces).exists())

        verifyNoMoreInteractions(s3Service)
    }

    fun readFile(fileName: String): String = File(fileName).readText(Charsets.UTF_8)

    companion object {

        private lateinit var xsdResource: Resource

        private val namespaces = HashMap<String, String>()

        private const val sqsMessageId = "a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02"

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            // namespaces["ns35"] = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
            namespaces["ns3"] = "http://www.justice.gov.uk/magistrates/ack"
            namespaces["env"] = "http://www.w3.org/2003/05/soap-envelope"
            val resourceLoader: ResourceLoader = DefaultResourceLoader()
            xsdResource = resourceLoader.getResource("xsd/generic/Acknowledgement/Acknowledgement.xsd")
        }
    }
}
