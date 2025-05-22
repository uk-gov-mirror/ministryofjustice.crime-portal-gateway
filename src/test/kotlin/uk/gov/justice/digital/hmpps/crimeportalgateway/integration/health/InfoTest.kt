package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.TestMessagingConfig
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Import(TestMessagingConfig::class)
class InfoTest : IntegrationTestBase() {
    @Test
    fun `Info page is accessible`() {
        webTestClient
            .get()
            .uri("/info")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("build.name")
            .isEqualTo("crime-portal-gateway")
    }

    @Test
    fun `Info page reports version`() {
        webTestClient
            .get()
            .uri("/info")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("build.version")
            .value<String> {
                assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
            }
    }
}
