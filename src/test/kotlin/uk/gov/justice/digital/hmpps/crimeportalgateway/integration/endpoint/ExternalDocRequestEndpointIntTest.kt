package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.ws.test.server.RequestCreators
import org.springframework.ws.test.server.ResponseMatchers.noFault
import org.springframework.ws.test.server.ResponseMatchers.validPayload
import org.springframework.ws.test.server.ResponseMatchers.xpath
import org.springframework.xml.transform.StringSource
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.transform.Source

class ExternalDocRequestEndpointIntTest : IntegrationTestBase() {
    @Autowired
    private lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var amazonS3: S3Client

    @Autowired
    lateinit var hmppsQueueService: HmppsQueueService
    val courtCasesQueue by lazy {
        hmppsQueueService.findByQueueId("courtcasesqueue")
    }

    @Value("\${aws.s3.bucket_name}")
    lateinit var bucketName: String

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun beforeEach() {
        courtCasesQueue?.sqsClient?.purgeQueue(PurgeQueueRequest.builder().queueUrl(courtCasesQueue!!.queueUrl).build())
        amazonS3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
    }

    @AfterEach
    fun afterEach() {
        val objectListing = amazonS3.listObjects(ListObjectsRequest.builder().bucket(bucketName).build())
        objectListing.contents()?.forEach {
            amazonS3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(it.key()).build())
        }

        amazonS3.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build())
    }

    @Test
    fun `should send SQS message for each case`() {
        val externalDoc1 = readFile("src/test/resources/soap/sample-request.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)
        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment"),
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success"),
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        verify(telemetryService).trackEvent(
            COURT_LIST_MESSAGE_RECEIVED,
            mapOf(
                "courtCode" to "B10JQ",
                "courtRoom" to "0",
                "hearingDate" to "2020-10-26",
                "fileName" to "5_26102020_2992_B10JQ00_ADULT_COURT_LIST_DAILY",
            ),
        )

        val firstCase = CaseDetails(caseNo = 166662981, defendantName = "MR Abraham LINCOLN", pnc = "20030011985X", cro = "CR0006100061")
        val secondCase = CaseDetails(caseNo = 1777732980, defendantName = "Mr Theremin MELLOTRON", pnc = "20120052494Q", cro = "CR0006200062")
        checkMessage(listOf(firstCase, secondCase))

        checkS3Upload("2020-10-26-B10")
    }

    @Test
    fun `when two duplicate messages received containing two cases_only one message per case ends up on the queue`() {
        val externalDoc1 = readFile("src/test/resources/soap/sample-request.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)
        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment"),
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success"),
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())
        // send duplicate message
        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment"),
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success"),
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        verify(telemetryService, times(2)).trackEvent(
            COURT_LIST_MESSAGE_RECEIVED,
            mapOf(
                "courtCode" to "B10JQ",
                "courtRoom" to "0",
                "hearingDate" to "2020-10-26",
                "fileName" to "5_26102020_2992_B10JQ00_ADULT_COURT_LIST_DAILY",
            ),
        )

        val firstCase = CaseDetails(caseNo = 166662981, defendantName = "MR Abraham LINCOLN", pnc = "20030011985X", cro = "CR0006100061")
        val secondCase = CaseDetails(caseNo = 1777732980, defendantName = "Mr Theremin MELLOTRON", pnc = "20120052494Q", cro = "CR0006200062")

        checkMessage(listOf(firstCase, secondCase))
        checkQueueIsEmpty()

        checkS3Upload("2020-10-26-B10")
    }

    @Test
    fun `should not enqueue message when court is not processed but return acknowledgement`() {
        val externalDoc1 = readFile("src/test/resources/soap/ignored-courts.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment"),
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success"),
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        checkQueueIsEmpty()
        checkS3Upload("2020-10-26-B10")
    }

    @Test
    fun `given no court present`() {
        val requestEnvelope: Source = StringSource(readFile("src/test/resources/soap/sample-request-invalid-xml.xml"))

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment"),
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success"),
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        checkQueueIsEmpty()
        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        checkS3Upload("fail-$date")
    }

    private fun checkQueueIsEmpty() {
        val numberOfMessagesOnQueue = courtCasesQueue?.sqsClient?.countMessagesOnQueue(courtCasesQueue?.queueUrl!!)?.get()!!
        assertThat(numberOfMessagesOnQueue).isEqualTo(0)
    }

    private fun countMessagesOnQueue() = courtCasesQueue?.sqsClient?.countMessagesOnQueue(courtCasesQueue?.queueUrl!!)!!.get()

    private fun checkS3Upload(fileNameStart: String) {
        val items = amazonS3.listObjects(ListObjectsRequest.builder().bucket(bucketName).build()).contents()
        assertThat(items[0].key().startsWith(fileNameStart)).isTrue()
        val s3Object = amazonS3.getObject(GetObjectRequest.builder().bucket(bucketName).key(items[0].key()).build(), ResponseTransformer.toBytes())
        val startOfDoc = s3Object.asUtf8String()

        assertThat(
            @Suppress("ktlint:standard:max-line-length")
            startOfDoc.startsWith(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:ExternalDocumentRequest xmlns:ns2=\"http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest\"",
            ),
        ).isTrue()
    }

    private fun readFile(fileName: String): String = File(fileName).readText(Charsets.UTF_8)

    private fun checkMessage(expectedCases: List<CaseDetails>) {
        val cases = mutableListOf<CaseDetails>()
        await().until { countMessagesOnQueue() == 2 }
        while (countMessagesOnQueue() > 0) {
            val message = courtCasesQueue?.sqsClient?.receiveMessage(ReceiveMessageRequest.builder().queueUrl(courtCasesQueue?.queueUrl!!).build())!!.get()
            val sqsMessage: SQSMessage = objectMapper.readValue(message.messages()[0].body(), SQSMessage::class.java)

            cases.add(objectMapper.readValue(sqsMessage.message, CaseDetails::class.java))
            courtCasesQueue?.sqsClient?.deleteMessage(
                DeleteMessageRequest.builder().queueUrl(courtCasesQueue?.queueUrl!!).receiptHandle(message.messages()[0].receiptHandle()).build(),
            )
        }
        assertThat(cases).containsAll(expectedCases)
        assertThat(cases).containsOnly(expectedCases[0], expectedCases[1])
    }

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class SQSMessage(
    @JsonProperty("Message")
    val message: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CaseDetails(
    val caseNo: Int,
    val defendantName: String,
    val cro: String,
    val pnc: String,
)
