package uk.gov.justice.digital.hmpps.crimeportalgateway.config

import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
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

@EnableWs
@Configuration
class WebServiceConfig : WsConfigurerAdapter() {

    override fun addInterceptors(interceptors: MutableList<EndpointInterceptor?>) {
        interceptors.add(SoapHeaderAddressInterceptor())
    }

    @Bean
    fun externalDocumentRequestWsdl(): SimpleWsdl11Definition? {
        return SimpleWsdl11Definition(ClassPathResource(EXT_REQ_XSD))
    }

    @Bean
    fun messageDispatcherServlet(applicationContext: ApplicationContext): ServletRegistrationBean<*>? {
        val servlet = MessageDispatcherServlet()
        servlet.setApplicationContext(applicationContext)
        servlet.isTransformWsdlLocations = true
        return ServletRegistrationBean(servlet, "/crime-portal-gateway/ws/*")
    }

    @Bean
    fun messageFactory(): SaajSoapMessageFactory? {
        val messageFactory = SaajSoapMessageFactory()
        messageFactory.setSoapVersion(SoapVersion.SOAP_12)
        return messageFactory
    }

    @Bean(name = ["ExternalDocumentRequest"])
    fun wsdl11Definition(requestSchema: XsdSchema?): DefaultWsdl11Definition? {
        val wsdl11Definition = DefaultWsdl11Definition()
        wsdl11Definition.setPortTypeName("WebServicePort")
        wsdl11Definition.setLocationUri("/crime-portal-gateway/")
        wsdl11Definition.setTargetNamespace("http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest")
        //        wsdl11Definition.setSchemaCollection(getXsds());
        wsdl11Definition.setSchema(requestSchema)
        return wsdl11Definition
    }

    @Bean
    fun requestSchema(): XsdSchema? {
        return SimpleXsdSchema(ClassPathResource(EXT_REQ_XSD))
    }

    companion object {
        const val EXT_REQ_XSD = "xsd/cp/External/ExternalDocumentRequest.xsd"
    }
}
