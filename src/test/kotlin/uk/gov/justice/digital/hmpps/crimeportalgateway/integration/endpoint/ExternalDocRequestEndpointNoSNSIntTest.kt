package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.ws.test.server.RequestCreators
import org.springframework.ws.test.server.ResponseMatchers
import org.springframework.ws.test.server.ResponseMatchers.xpath
import org.springframework.xml.transform.StringSource
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.io.File
import javax.xml.transform.Source

class ExternalDocRequestEndpointNoSNSIntTest : IntegrationTestBase() {
    @MockBean
    private lateinit var amazonSNS: AmazonSNS

    @Test
    fun `given no SNS available then SOAP Fault`() {
        val soapDocument = readFile("src/test/resources/soap/sample-request.xml")
        val requestEnvelope: Source = StringSource(soapDocument)

        whenever(amazonSNS.publish(any(PublishRequest::class.java))).thenThrow(IllegalArgumentException())
        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(ResponseMatchers.serverOrReceiverFault())
            .andExpect(xpath("//env:Fault/env:Code/env:Value", namespaces).exists())

        // check no message on queue??
        // possible s3 check - should have been written
    }

    fun readFile(fileName: String): String = File(fileName).readText(Charsets.UTF_8)

    companion object {
        private lateinit var xsdResource: Resource

        private val namespaces = HashMap<String, String>()

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
