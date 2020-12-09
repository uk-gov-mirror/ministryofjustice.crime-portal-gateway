package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileSystemResource
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean
import java.io.IOException

@Profile("secure")
@Configuration
class WebServiceSecurityConfig(
    @Value("\${keystore-password}") private val keystorePassword: String,
    @Value("\${ws-sec.request-encrypt-actions}") private val requestActions: String,
    @Value("\${ws-sec.response-encrypt-actions}") private val responseActions: String,
    @Value("\${ws-sec.response-signature-parts}") private val responseSignatureParts: String,
    @Value("\${ws-sec.response-encryption-parts}") private val responseEncryptionParts: String,
    @Value("\${ws-sec.encryption-sym-algorithm}") private val encryptionSymAlgorithm: String,
    @Value("\${trusted_cert_alias_name}") private val trustedCertAliasName: String,
    @Value("\${keystore-file-path}") private val keystoreFilePath: String,
    @Value("\${private_key_alias_name}") private val privateKeyAliasName: String,
) {

    @Bean
    fun securityCallbackHandler(): KeyStoreCallbackHandler {
        val callbackHandler = KeyStoreCallbackHandler()
        callbackHandler.setPrivateKeyPassword(keystorePassword)
        return callbackHandler
    }

    @Bean
    @Throws(IOException::class)
    fun getCryptoFactoryBean(): CryptoFactoryBean {
        val cryptoFactoryBean = CryptoFactoryBean()
        cryptoFactoryBean.setKeyStorePassword(keystorePassword)
        cryptoFactoryBean.setKeyStoreLocation(FileSystemResource(keystoreFilePath))
        return cryptoFactoryBean
    }

    @Bean
    @Throws(Exception::class)
    fun securityInterceptor(): Wss4jSecurityInterceptor {
        val securityInterceptor = Wss4jSecurityInterceptor()

        val cryptoBean = getCryptoFactoryBean().getObject()

        // validate incoming request
        securityInterceptor.setValidationActions(requestActions)
        securityInterceptor.setValidationSignatureCrypto(cryptoBean)
        securityInterceptor.setValidationDecryptionCrypto(cryptoBean)
        securityInterceptor.setValidationCallbackHandler(securityCallbackHandler())

        // encrypt the response
        securityInterceptor.setSecurementEncryptionUser(trustedCertAliasName)
        securityInterceptor.setSecurementEncryptionSymAlgorithm(encryptionSymAlgorithm)
        securityInterceptor.setSecurementEncryptionParts(responseEncryptionParts)
        securityInterceptor.setSecurementEncryptionCrypto(getCryptoFactoryBean().getObject())

        // sign the response
        securityInterceptor.setSecurementActions(responseActions)
        securityInterceptor.setSecurementUsername(privateKeyAliasName)
        securityInterceptor.setSecurementPassword(keystorePassword)
        securityInterceptor.setSecurementSignatureParts(responseSignatureParts)
        securityInterceptor.setSecurementSignatureCrypto(cryptoBean)
        return securityInterceptor
    }
}
