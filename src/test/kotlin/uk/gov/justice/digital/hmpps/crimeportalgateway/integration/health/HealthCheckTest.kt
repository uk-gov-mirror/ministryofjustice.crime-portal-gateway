package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyBoolean
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.MessagingConfigTest
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.healthchecks.SqsCheck
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

@MockBean(SqsCheck::class)
@Import(MessagingConfigTest::class)
class HealthCheckTest : IntegrationTestBase() {

    @Autowired
    private lateinit var sqsCheck: SqsCheck

    @BeforeEach
    fun beforeEach() {
        whenever(sqsCheck.getHealth(anyBoolean())).thenReturn(Mono.just(Health.Builder().up().build()))
    }

    @Test
    fun `Health page reports ok`() {
        webTestClient.get()
            .uri("/health")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("status").isEqualTo("UP")
    }

    @Test
    fun `Health info reports version`() {

        webTestClient.get().uri("/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("components.healthInfo.details.version")
            .value(
                Consumer<String> {
                    assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
                }
            )
    }

    @Test
    fun `Health ping page is accessible`() {
        webTestClient.get()
            .uri("/health/ping")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("status").isEqualTo("UP")
    }

    @Test
    fun `readiness reports ok`() {
        webTestClient.get()
            .uri("/health/readiness")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("status").isEqualTo("UP")
    }

    @Test
    fun `liveness reports ok`() {
        webTestClient.get()
            .uri("/health/liveness")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("status").isEqualTo("UP")
    }
}
