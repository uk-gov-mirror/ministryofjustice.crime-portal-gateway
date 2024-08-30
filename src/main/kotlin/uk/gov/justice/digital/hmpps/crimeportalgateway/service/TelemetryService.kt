package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Case
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Info

@Component
class TelemetryService(
    @Autowired private val telemetryClient: TelemetryClient,
) {
    fun trackEvent(eventType: TelemetryEventType) {
        telemetryClient.trackEvent(eventType.eventName)
    }

    fun trackEvent(
        eventType: TelemetryEventType,
        customDimensions: Map<String, String?>,
    ) {
        telemetryClient.trackEvent(eventType.eventName, customDimensions, null)
    }

    fun trackCourtListEvent(info: Info) {
        val properties = mapOf(COURT_CODE_KEY to info.ouCode, HEARING_DATE_KEY to info.dateOfHearing.toString())

        telemetryClient.trackEvent(TelemetryEventType.COURT_LIST_RECEIVED.eventName, properties, emptyMap())
    }

    fun trackCourtCaseSplitEvent(
        case: Case,
        messageId: String,
    ) {
        val session = case.block.session
        val properties =
            mapOf(
                COURT_CODE_KEY to session.courtCode,
                COURT_ROOM_KEY to session.courtRoom,
                HEARING_DATE_KEY to session.dateOfHearing.toString(),
                CASE_NO_KEY to case.caseNo,
                SQS_MESSAGE_ID_KEY to messageId,
            )

        telemetryClient.trackEvent(TelemetryEventType.COURT_CASE_SPLIT.eventName, properties, emptyMap())
    }

    companion object {
        const val COURT_CODE_KEY = "courtCode"
        const val COURT_ROOM_KEY = "courtRoom"
        const val CASE_NO_KEY = "caseNo"
        const val HEARING_DATE_KEY = "hearingDate"
        const val SQS_MESSAGE_ID_KEY = "sqsMessageId"
    }
}
