package uk.gov.justice.digital.hmpps.crimeportalgateway.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Ping {
  @GetMapping("/ping")
  fun ping(): String {
    return "pong"
  }
}
