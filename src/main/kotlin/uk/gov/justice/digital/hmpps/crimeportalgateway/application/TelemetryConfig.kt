package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils
import com.microsoft.applicationinsights.telemetry.RequestTelemetry
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext
import com.microsoft.applicationinsights.web.internal.ThreadContext
import org.springframework.context.annotation.*
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.lang.NonNull
import org.springframework.web.context.annotation.RequestScope
import java.util.*

@Configuration
class TelemetryConfig {

    @Bean
    @Conditional(AppInsightKeyAbsentCondition::class)
    fun getTelemetryClient(): TelemetryClient {
        return TelemetryClient()
    }

    private class AppInsightKeyAbsentCondition : Condition {
        override fun matches(@NonNull context: ConditionContext, @NonNull metadata: AnnotatedTypeMetadata): Boolean {
            return StringUtils.isEmpty(context.environment.getProperty("application.insights.ikey"))
        }
    }

    @Bean
    @Profile("!test")
    @RequestScope
    fun requestProperties(): Map<String, String> {
        return Optional.ofNullable(ThreadContext.getRequestTelemetryContext())
            .map { obj: RequestTelemetryContext -> obj.httpRequestTelemetry }
            .map { obj: RequestTelemetry -> obj.properties }
            .orElse(emptyMap())
    }
}
