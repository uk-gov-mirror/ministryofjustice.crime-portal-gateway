package uk.gov.justice.digital.hmpps.crimeportalgateway.application.healthchecks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService

@Component
class SqsCheck(@Autowired private val sqsService: SqsService) : ReactiveHealthIndicator {

    override fun health(): Mono<Health> {
        var health = Health.Builder().down().build()
        if (sqsService.isQueueAvailable()) {
            health = Health.Builder().up().build()
        }
        return Mono.just(health)
    }
}
