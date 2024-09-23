package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileSystemResource
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean

@Profile("secure")
@Configuration
class WebServiceSecurityConfig(
    @Value("\${keystore-password}") private val keystorePassword: String,
    @Value("\${private-key-password}") private val privateKeyPassword: String,
    @Value("\${ws-sec.request-encrypt-actions}") private val requestActions: String,
    @Value("\${ws-sec.response-encrypt-actions}") private val responseActions: String,
    @Value("\${ws-sec.response-signature-parts}") private val responseSignatureParts: String,
    @Value("\${ws-sec.response-encryption-parts}") private val responseEncryptionParts: String,
    @Value("\${trusted-cert-alias-name}") private val trustedCertAliasName: String,
    @Value("\${private-key-alias-name}") private val privateKeyAliasName: String,
    @Value("\${ws-sec.keystore-file-path}") private val keystoreFilePath: String,
    @Value("\${ws-sec.encryption-sym-algorithm}") private val encryptionAlgorithm: String,
) {
    @Bean
    @Throws(Exception::class)
    fun keyStoreCallbackHandler(): KeyStoreCallbackHandler {
        val callbackHandler = KeyStoreCallbackHandler()
        callbackHandler.setPrivateKeyPassword(privateKeyPassword)
        return callbackHandler
    }

    @Bean
    fun getValidationCryptoFactoryBean(): CryptoFactoryBean {
        val cryptoFactoryBean = CryptoFactoryBean()
        cryptoFactoryBean.setKeyStoreLocation(FileSystemResource(keystoreFilePath))
        cryptoFactoryBean.setKeyStorePassword(keystorePassword)
        cryptoFactoryBean.setDefaultX509Alias(trustedCertAliasName)
        return cryptoFactoryBean
    }

    @Bean
    @Throws(Exception::class)
    fun securityInterceptor(): Wss4jSecurityInterceptor {
        val securityInterceptor = Wss4jSecurityInterceptor()

        // validate incoming request
        securityInterceptor.setValidationActions(requestActions)
        securityInterceptor.setValidationSignatureCrypto(getValidationCryptoFactoryBean().getObject())
        securityInterceptor.setValidationDecryptionCrypto(getValidationCryptoFactoryBean().getObject())
        securityInterceptor.setValidationCallbackHandler(keyStoreCallbackHandler())

        // encrypt the response
        securityInterceptor.setSecurementEncryptionUser(privateKeyAliasName)
        securityInterceptor.setSecurementEncryptionParts(responseEncryptionParts)
        securityInterceptor.setSecurementEncryptionCrypto(getValidationCryptoFactoryBean().getObject())
        securityInterceptor.setSecurementEncryptionSymAlgorithm(encryptionAlgorithm)

        // sign the response
        securityInterceptor.setSecurementActions(responseActions)
        securityInterceptor.setSecurementUsername(trustedCertAliasName)
        securityInterceptor.setSecurementPassword(privateKeyPassword)
        securityInterceptor.setSecurementSignatureParts(responseSignatureParts)
        securityInterceptor.setSecurementSignatureCrypto(getValidationCryptoFactoryBean().getObject())
        return securityInterceptor
    }
}
