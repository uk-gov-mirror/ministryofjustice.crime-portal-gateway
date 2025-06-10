package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
<<<<<<< HEAD
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
=======
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
>>>>>>> aceada2 (Add swagger to crime-portal-gateway)
            .csrf { it.disable() }
            .authorizeHttpRequests { http ->
                http.requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/info/**",
                    "/health/**",
                    "/api-docs/**",
                ).permitAll()
            }
            .build()
<<<<<<< HEAD
<<<<<<< HEAD:src/main/kotlin/uk/gov/justice/digital/hmpps/crimeportalgateway/application/InfoSecurityConfig.kt
}
=======
    }
}
>>>>>>> aceada2 (Add swagger to crime-portal-gateway):src/main/kotlin/uk/gov/justice/digital/hmpps/crimeportalgateway/application/SecurityConfig.kt
=======
    }
}
>>>>>>> aceada2 (Add swagger to crime-portal-gateway)
