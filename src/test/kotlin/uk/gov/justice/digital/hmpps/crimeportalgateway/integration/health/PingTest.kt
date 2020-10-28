package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase

class PingTest : IntegrationTestBase() {

  @Test
  fun `Ping page reports pong`() {
    webTestClient.get()
        .uri("/ping")
        .exchange()
        .expectStatus()
        .isOk
  }

}
