package uk.gov.justice.digital.hmpps.crimeportalgateway.messaging

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import javax.validation.ConstraintViolationException
import javax.validation.Validator

class MessageParser<T>(
    @Qualifier("messageXmlMapper") private val xmlMapper: XmlMapper,
    @Autowired private val validator: Validator
) {

    @Throws(JsonProcessingException::class)
    fun parseMessage(xml: String?, type: Class<T>): T {
        val javaType: JavaType = xmlMapper.typeFactory.constructType(type)
        val message: T = xmlMapper.readValue(xml, javaType)
        validate(message)
        return message
    }

    private fun validate(messageType: T) {
        val errors = validator.validate<Any>(messageType)
        if (errors.isNotEmpty()) {
            throw ConstraintViolationException(errors)
        }
    }

    companion object {
        const val EXT_DOC_NS = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
        const val CSCI_HDR_NS = "http://www.justice.gov.uk/magistrates/generic/CSCI_Header"
        const val CSCI_BODY_NS = "http://www.justice.gov.uk/magistrates/cp/CSCI_Body"
        const val CSC_STATUS_NS = "http://www.justice.gov.uk/magistrates/generic/CSCI_Status"
        const val GW_MSG_SCHEMA = "http://www.justice.gov.uk/magistrates/cp/GatewayMessageSchema"
    }
}
