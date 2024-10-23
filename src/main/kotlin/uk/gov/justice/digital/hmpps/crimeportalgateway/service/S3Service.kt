package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.MessageDetail

@Component
class S3Service(
    @Value("\${aws.s3.bucket_name}") private val bucketName: String,
    @Autowired private val amazonS3Client: S3Client,
) {
    fun uploadMessage(
        messageDetail: MessageDetail,
        messageContent: String,
    ): String? {
        return uploadMessage(messageDetail.asFileNameStem() + ".xml", messageContent)
    }

    fun uploadMessage(
        fileName: String,
        messageContent: String,
    ): String? {
        return try {
            val putResult = amazonS3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName).build(), RequestBody.fromString(messageContent))
            log.info("File {} saved to S3 bucket {} with expiration date of {}, eTag {}", fileName, bucketName, putResult.expiration(), putResult.eTag())
            putResult.eTag()
        } catch (ex: RuntimeException) {
            // Happy to swallow this one with a log statement because failure to back up the file is not business critical
            log.error("Failed to back up file {} saved to S3 bucket {}", fileName, bucketName, ex)
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
