package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { http ->
                http
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/info/**",
                        "/health/**",
                        "/api-docs/**",
                    ).permitAll()
            }.build()
}
