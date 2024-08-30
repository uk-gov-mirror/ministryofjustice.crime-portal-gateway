package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageParser.Companion.EXT_DOC_NS
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class ExternalDocumentRequest(
    @field:NotNull
    @field:Valid
    @JacksonXmlProperty(namespace = EXT_DOC_NS, localName = "documents")
    val documentWrapper: DocumentWrapper,
)
