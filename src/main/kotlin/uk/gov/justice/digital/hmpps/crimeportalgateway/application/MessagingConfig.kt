package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageParser
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.ExternalDocumentRequest
import javax.validation.Validation

@Configuration
class MessagingConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerKotlinModule()
        return objectMapper
    }

    @Bean(name = ["messageXmlMapper"])
    fun xmlMapper(): XmlMapper {
        val xmlModule = JacksonXmlModule()
        xmlModule.setDefaultUseWrapper(false)
        val mapper = XmlMapper(xmlModule)
        mapper.registerKotlinModule()
        mapper.registerModule(JavaTimeModule())
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    @Bean
    fun messageParser(xmlMapper: XmlMapper): MessageParser<ExternalDocumentRequest> {
        return MessageParser(xmlMapper, Validation.buildDefaultValidatorFactory().validator)
    }
}
