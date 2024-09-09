package uk.gov.justice.digital.hmpps.crimeportalgateway.xml

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MessageDetailTest {
    @Test
    fun `as filename`() {
        val messageDetail = MessageDetail(courtCode = "B10JQ", courtRoom = 0, hearingDate = "2020-10-26")

        assertThat(messageDetail.asFileNameStem()).startsWith("2020-10-26-B10JQ-0")
    }
}
