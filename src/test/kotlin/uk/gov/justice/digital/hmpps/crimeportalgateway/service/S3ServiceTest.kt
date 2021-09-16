package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectResult
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.MessageDetail
import java.util.Date

@ExtendWith(MockitoExtension::class)
internal class S3ServiceTest {

    @Mock
    lateinit var amazonS3Client: AmazonS3Client

    private lateinit var s3Service: S3Service

    @BeforeEach
    fun setUp() {
        s3Service = S3Service("bucket-name", amazonS3Client)
    }

    @Test
    fun `given normal input then message is uploaded as file`() {

        val messageDetail = MessageDetail(
            hearingDate = "29-08-2016",
            courtCode = "B10JQ",
            courtRoom = 2
        )

        val putResult: PutObjectResult = PutObjectResult().apply {
            expirationTime = Date()
            eTag = "ETAG"
        }
        whenever(amazonS3Client.putObject(eq("bucket-name"), any(), eq("message"))).thenReturn(putResult)

        val eTag = s3Service.uploadMessage(messageDetail, "message")

        verify(amazonS3Client).putObject(eq("bucket-name"), any(), eq("message"))
        assertThat(eTag).isEqualTo("ETAG")
    }
}
