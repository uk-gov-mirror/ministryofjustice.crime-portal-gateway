package uk.gov.justice.digital.hmpps.crimeportalgateway.service

enum class TelemetryEventType(val eventName: String) {
    COURT_LIST_MESSAGE_ERROR("PiCCourtListMessageError"),
    COURT_LIST_MESSAGE_RECEIVED("PiCCourtListMessageReceived"),
    COURT_LIST_MESSAGE_IGNORED("PiCCourtListMessageIgnored"),
    COURT_LIST_RECEIVED("PiCCourtListReceived"), // Records receipt of a list per court
    COURT_CASE_SPLIT("PiCCourtCaseSplit")
    ;
}
