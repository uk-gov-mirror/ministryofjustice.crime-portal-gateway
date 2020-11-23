package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.contains
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.ws.test.server.MockWebServiceClient
import org.springframework.ws.test.server.RequestCreators
import org.springframework.ws.test.server.ResponseMatchers.noFault
import org.springframework.ws.test.server.ResponseMatchers.serverOrReceiverFault
import org.springframework.ws.test.server.ResponseMatchers.validPayload
import org.springframework.ws.test.server.ResponseMatchers.xpath
import org.springframework.xml.transform.StringSource
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.MessagingConfigTest
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import javax.xml.transform.Source

@Import(MessagingConfigTest::class)
class ExternalDocRequestEndpointIntTest : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var sqsService: SqsService

    private lateinit var mockClient: MockWebServiceClient

    @BeforeEach
    fun before() {
        mockClient = MockWebServiceClient.createClient(applicationContext)
    }

    @Test
    fun `should enqueue message and return successful acknowledgement`() {
        val requestEnvelope: Source = StringSource(externalDocRequest)

        whenever(sqsService.enqueueMessage(contains("ExternalDocumentRequest"))).thenReturn(sqsMessageId)

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/ackType/MessageComment", namespaces)
                    .evaluatesTo("Message successfully enqueued with id $sqsMessageId")
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/ackType/MessageStatus", namespaces)
                    .evaluatesTo("Success")
            )
            .andExpect(xpath("//ns3:Acknowledgement/ackType/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        verify(telemetryService).trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)
        verify(sqsService).enqueueMessage(anyString())
    }

    @Test
    fun `given invalid ExternalDocumentRequest should provide SOAP fault `() {
        val requestEnvelope: Source = StringSource(externalDocRequest.replace("<documents></documents>", ""))

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(serverOrReceiverFault())
            .andExpect(xpath("//env:Fault/env:Code/env:Value", namespaces).exists())
    }

    companion object {

        private lateinit var xsdResource: Resource

        private val namespaces = HashMap<String, String>()

        private const val sqsMessageId = "a4e9ab53-f8aa-bf2c-7291-d0293a8b0d02"

        private const val externalDocRequest: String =
            """<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ns35="http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest">\n>
                <soap:Header xmlns:wsa="http://www.w3.org/2005/08/addressing">\n
                   <wsa:Action>externalDocument</wsa:Action>\n 
                   <wsa:From>\n
                         <wsa:Address>CP_NPS_ML</wsa:Address>\n
                   </wsa:From>\n
                      <wsa:MessageID>09233523-345b-4351-b623-5dsf35sgs5d6</wsa:MessageID>\n
                      <wsa:RelatesTo>RelatesToValue</wsa:RelatesTo>\n
                      <wsa:To>CP_NPS</wsa:To>\n
                </soap:Header>\n
                <soap:Body>\n
                   <ns35:ExternalDocumentRequest><documents></documents></ns35:ExternalDocumentRequest>\n
                </soap:Body>\n
                </soap:Envelope>"""

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            namespaces["ns3"] = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
            namespaces["env"] = "http://www.w3.org/2003/05/soap-envelope"
            val resourceLoader: ResourceLoader = DefaultResourceLoader()
            xsdResource = resourceLoader.getResource("xsd/cp/external/ExternalDocumentRequest.xsd")
        }
    }
}
