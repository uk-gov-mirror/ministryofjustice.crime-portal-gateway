package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.MessagingConfigTest
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Import(MessagingConfigTest::class)
class InfoTest : IntegrationTestBase() {

    @Test
    fun `Info page is accessible`() {
        webTestClient.get()
            .uri("/info")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("app.name").isEqualTo("Crime Portal Gateway")
    }

    @Test
    fun `Info page reports version`() {
        webTestClient.get().uri("/info")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("build.version").value<String> {
                assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
            }
    }
}
