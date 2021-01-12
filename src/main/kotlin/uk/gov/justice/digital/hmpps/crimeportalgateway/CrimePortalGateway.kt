package uk.gov.justice.digital.hmpps.crimeportalgateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.ws.config.annotation.EnableWs

@EnableWs
@SpringBootApplication
class CrimePortalGateway

fun main(args: Array<String>) {
    runApplication<CrimePortalGateway>(*args)
}
