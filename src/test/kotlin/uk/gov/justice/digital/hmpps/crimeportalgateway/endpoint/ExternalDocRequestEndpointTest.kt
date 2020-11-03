package uk.gov.justice.digital.hmpps.crimeportalgateway.endpoint

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest

internal class ExternalDocRequestEndpointTest {

    private val endpoint = ExternalDocRequestEndpoint()

    @Test
    fun `given success should the correct acknowledgement message`() {
        val request = ExternalDocumentRequest()

        val ack = endpoint.processRequest(request)

        assertThat(ack).isNotNull
        assertThat(ack.ackType.messageComment).isEqualTo("MessageComment")
        assertThat(ack.ackType.messageStatus).isEqualTo("MessageStatus")
        assertThat(ack.ackType.timeStamp).isNotNull
    }

}
