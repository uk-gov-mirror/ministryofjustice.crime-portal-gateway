package uk.gov.justice.digital.hmpps.crimeportalgateway.application.healthchecks

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import org.mockito.Mockito.`when` as mockitoWhen

@ExtendWith(MockitoExtension::class)
internal class SqsCheckTest {

    @Mock
    private lateinit var sqsService: SqsService

    @InjectMocks
    private lateinit var sqsCheck: SqsCheck

    @Test
    fun `should give UP status when queue is available`() {

        mockitoWhen(sqsService.isQueueAvailable()).thenReturn(TRUE)

        val health: Health = sqsCheck.health().block()

        assertThat(health.status).isSameAs(Status.UP)
    }

    @Test
    fun `should give DOWN status when queue is available`() {

        mockitoWhen(sqsService.isQueueAvailable()).thenReturn(FALSE)

        val health: Health = sqsCheck.health().block()

        assertThat(health.status).isSameAs(Status.DOWN)
    }
}
