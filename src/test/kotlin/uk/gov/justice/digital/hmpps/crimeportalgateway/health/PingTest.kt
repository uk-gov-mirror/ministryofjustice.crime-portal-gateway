package uk.gov.justice.digital.hmpps.crimeportalgateway.health

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class PingTest {

    private val ping = Ping()

    @Test
    fun `Ping returns pong`() {
        Assertions.assertThat(ping.ping()).isEqualTo("pong")
    }
}
