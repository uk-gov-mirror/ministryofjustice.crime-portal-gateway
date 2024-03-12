package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.lang.NonNull

open class BaseTelemetryConfig {

    class AppInsightKeyAbsentCondition : Condition {
        override fun matches(@NonNull context: ConditionContext, @NonNull metadata: AnnotatedTypeMetadata): Boolean {
            return StringUtils.isEmpty(context.environment.getProperty("application.insights.ikey"))
        }
    }
}
