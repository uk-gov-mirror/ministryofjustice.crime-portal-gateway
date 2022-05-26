package uk.gov.justice.digital.hmpps.crimeportalgateway.application.healthchecks

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService

@ExtendWith(MockitoExtension::class)
internal class SqsCheckTest {

    @Mock
    private lateinit var sqsService: SqsService

    @InjectMocks
    private lateinit var sqsCheck: SqsCheck

    @Test
    fun `should give UP status when queue is available`() {

        whenever(sqsService.isQueueAvailable()).thenReturn(true)

        val health: Health = sqsCheck.health().block()

        assertThat(health.status).isSameAs(Status.UP)
    }

    @Test
    fun `should give DOWN status when queue is available`() {

        whenever(sqsService.isQueueAvailable()).thenReturn(false)

        val health: Health = sqsCheck.health().block()

        assertThat(health.status).isSameAs(Status.DOWN)
    }
}
