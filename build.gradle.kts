import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.1.0"
    kotlin("plugin.spring") version "2.1.21"
    id("org.unbroken-dome.xjc") version "2.0.0"
    kotlin("jvm") version "2.1.21"
}

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencyCheck {
    suppressionFiles.add("cpg-suppressions.xml")
}

dependencies {

    implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.2.1")

    implementation("org.springframework.ws:spring-ws-security:4.0.14") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
            .because("OWASP found security Issues")
        exclude(group = "org.cryptacular", module = "cryptacular")
            .because("OWASP found security Issues")
        exclude(group = "org.apache.santuario", module = "xmlsec")
            .because("OWASP found security Issues")
        implementation("org.apache.santuario:xmlsec:3.0.0")
    }

    implementation("com.microsoft.azure:applicationinsights-web:3.5.4")

    api("software.amazon.awssdk:s3")
    implementation("wsdl4j:wsdl4j:1.6.3")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.5") {
        exclude(group = "com.sun.xml.bind", module = "jaxb-core")
    }
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")

    xjcTool("com.sun.xml.bind:jaxb-xjc:3.0.2")
    xjcTool("com.sun.xml.bind:jaxb-impl:3.0.2")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.2.5")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")

    testImplementation("org.springframework.ws:spring-ws-test:4.0.11")
}

xjc {
    srcDirName.set("resources/xsd")
    extension.set(true)
    xjcVersion.set("3.0")
}

sourceSets.named("main") {
    xjcBinding.srcDir("resources/xsd")
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
        jvmTarget = JvmTarget.JVM_21
    }
}
