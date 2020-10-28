package uk.gov.justice.digital.hmpps.crimeportalgateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CrimePortalGateway

fun main(args: Array<String>) {
  runApplication<CrimePortalGateway>(*args)
}
