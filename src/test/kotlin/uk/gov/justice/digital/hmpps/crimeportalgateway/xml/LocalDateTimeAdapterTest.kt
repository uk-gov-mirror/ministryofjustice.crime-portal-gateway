package uk.gov.justice.digital.hmpps.crimeportalgateway.xml

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month

internal class LocalDateTimeAdapterTest {

    private val adapter = LocalDateTimeAdapter()

    @Test
    fun `should create string from date time`() {
        val time = LocalDateTime.of(1969, Month.AUGUST, 26, 13, 10, 10)
        val timeStr = adapter.marshal(time)
        assertThat(timeStr).isEqualTo("1969-08-26T13:10:10")
    }

    @Test
    fun `should create datetime from string`() {
        val timeStr = "1969-08-26T09:11:11"
        val expectedTime = LocalDateTime.of(1969, Month.AUGUST, 26, 9, 11, 11)

        assertThat(adapter.unmarshal(timeStr)).isEqualTo(expectedTime)
    }
}
