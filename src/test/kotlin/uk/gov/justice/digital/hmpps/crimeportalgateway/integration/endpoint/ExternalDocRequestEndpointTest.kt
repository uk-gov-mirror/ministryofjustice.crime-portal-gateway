package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.ws.test.server.MockWebServiceClient
import org.springframework.ws.test.server.RequestCreators
import org.springframework.xml.transform.StringSource
import uk.gov.justice.digital.hmpps.crimeportalgateway.config.WebServiceConfig
import javax.xml.transform.Source

//@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest
@ContextConfiguration(classes = [WebServiceConfig::class])
internal class ExternalDocRequestEndpointTest {

    // https://docs.spring.io/spring-ws/docs/current/reference/#_server_side_testing

    @Autowired
    private val applicationContext: ApplicationContext? = null

    private var mockClient: MockWebServiceClient? = null

    @Before
    fun before() {
        mockClient = MockWebServiceClient.createClient(applicationContext)
    }

    @Ignore
    @Test
    fun `should return SOAP header and body`() {
        val requestEnvelope: Source = StringSource(
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" \n"
                        + "\t\t\txmlns:ns35=\"http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest\">\n"
                        + "   <soap:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n"
                        + "      <wsa:Action>externalDocument</wsa:Action>\n"
                        + "      <wsa:From>\n"
                        + "         <wsa:Address>CP_NPS_ML</wsa:Address>\n"
                        + "      </wsa:From>\n"
                        + "      <wsa:MessageID>09233523-345b-4351-b623-5dsf35sgs5d6</wsa:MessageID>\n"
                        + "      <wsa:RelatesTo>RelatesToValue</wsa:RelatesTo>\n"
                        + "      <wsa:To>CP_NPS</wsa:To>\n"
                        + "   </soap:Header>\n"
                        + "   <soap:Body>\n"
                        + "      <ns35:ExternalDocumentRequest><documents></documents>"
                        + "      </ns35:ExternalDocumentRequest>\n"
                        + "   </soap:Body>\n"
                        + "</soap:Envelope>")


        val actions = mockClient!!.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
//        actions.andExpect(ResponseMatcher)
    }

}
