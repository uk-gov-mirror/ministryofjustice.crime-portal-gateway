package uk.gov.justice.digital.hmpps.crimeportalgateway.service

enum class TelemetryEventType(val eventName: String) {
    COURT_LIST_MESSAGE_ERROR("PiCCourtListMessageError"),
    COURT_LIST_MESSAGE_RECEIVED("PiCCourtListMessageReceived")
    ;
}
