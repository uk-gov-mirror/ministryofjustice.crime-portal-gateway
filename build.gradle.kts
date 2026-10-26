import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.14"
    kotlin("plugin.spring") version "2.4.21"
    kotlin("jvm") version "2.4.21"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://build.shibboleth.net/maven/releases/")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencyCheck {
    suppressionFiles.add("cpg-suppressions.xml")
}

dependencies {
    implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.1")
    implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-autoconfigure:7.4.1")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.ws:spring-ws-security:5.0.3") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk18on")
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk18on")
        exclude(group = "org.bouncycastle", module = "bcutil-jdk18on")
    }
    implementation("com.microsoft.azure:applicationinsights-web:3.7.10")

    api("software.amazon.awssdk:s3")
    implementation("wsdl4j:wsdl4j:1.6.3")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.6") {
        exclude(group = "com.sun.xml.bind", module = "jaxb-core")
    }
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.5")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.22.4")
    implementation("io.sentry:sentry-spring-boot-4-starter:8.61.1")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.3.2")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.6")

    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")

    testImplementation("org.springframework.ws:spring-ws-test:5.0.3")
}

tasks {
    register<Copy>("copyAgentConfig") {
        description = "Copy applicationinsights.json to build.lib so App Insights config is applied correctly"
        from("applicationinsights.json")
        into("$buildDir/libs")
    }
}

tasks.named("assemble") {
    dependsOn("copyAgentConfig")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
    }
}
