package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.web.context.WebApplicationContext
import org.springframework.ws.config.annotation.EnableWs
import org.springframework.ws.config.annotation.WsConfigurerAdapter
import org.springframework.ws.server.EndpointInterceptor
import org.springframework.ws.soap.SoapVersion
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory
import org.springframework.ws.transport.http.MessageDispatcherServlet
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition
import org.springframework.xml.xsd.SimpleXsdSchema
import org.springframework.xml.xsd.XsdSchema
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.io.File
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

@EnableWs
@Configuration
class WebServiceConfig(
    @Value("\${soap.ws-location-uri}") private val wsLocationUri: String,
    @Value("\${soap.target-namespace}") private val targetNamespace: String,
    @Value("\${soap.xsd-file-path}") private val xsdFilePath: String,
    @Autowired private val telemetryService: TelemetryService
) : WsConfigurerAdapter() {

    override fun addInterceptors(interceptors: MutableList<EndpointInterceptor>) {
        interceptors.add(SoapHeaderAddressInterceptor(telemetryService))
    }

    @Bean
    fun externalDocumentXsdResource(): Resource {
        return FileSystemResource(xsdFilePath)
    }

    @Bean
    fun externalDocumentRequestWsdl(externalDocumentXsdResource: Resource): SimpleWsdl11Definition {
        return SimpleWsdl11Definition(externalDocumentXsdResource)
    }

    @Bean
    fun messageDispatcherServlet(applicationContext: ApplicationContext): ServletRegistrationBean<*>? {
        return ServletRegistrationBean(
            MessageDispatcherServlet(applicationContext as WebApplicationContext).apply {
                setApplicationContext(applicationContext)
                isTransformWsdlLocations = true
            },
            "$wsLocationUri*"
        )
    }

    @Bean
    fun messageFactory(): SaajSoapMessageFactory {
        return SaajSoapMessageFactory().apply {
            setSoapVersion(SoapVersion.SOAP_12)
        }
    }

    @Bean(name = ["ExternalDocumentRequest"])
    fun wsdl11Definition(requestSchema: XsdSchema): DefaultWsdl11Definition? {
        return DefaultWsdl11Definition().apply {
            setPortTypeName("WebServicePort")
            setLocationUri(wsLocationUri)
            setTargetNamespace(targetNamespace)
            setSchema(requestSchema)
        }
    }

    @Bean
    fun requestSchema(externalDocumentXsdResource: Resource): XsdSchema {
        return SimpleXsdSchema(externalDocumentXsdResource)
    }

    @Bean
    fun validationSchema(externalDocumentXsdResource: Resource): Schema {
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        return schemaFactory.newSchema(File(xsdFilePath))
    }

    @Bean
    fun jaxbContext(): JAXBContext {
        return JAXBContext.newInstance(ExternalDocumentRequest::class.java)
    }
}
