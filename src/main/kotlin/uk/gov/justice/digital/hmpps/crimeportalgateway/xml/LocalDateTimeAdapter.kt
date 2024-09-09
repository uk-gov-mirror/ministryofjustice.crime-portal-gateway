package uk.gov.justice.digital.hmpps.crimeportalgateway.xml

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : XmlAdapter<String, LocalDateTime>() {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss")

    override fun unmarshal(value: String?): LocalDateTime {
        return LocalDateTime.parse(value)
    }

    override fun marshal(value: LocalDateTime?): String? {
        return value?.format(this.dateTimeFormatter)
    }
}
