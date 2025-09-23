import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.4"
    kotlin("plugin.spring") version "2.2.0"
    id("org.unbroken-dome.xjc") version "2.0.0"
    kotlin("jvm") version "2.2.0"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://build.shibboleth.net/maven/releases/")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencyCheck {
    suppressionFiles.add("cpg-suppressions.xml")
}

dependencies {

    implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.10")

    implementation("org.springframework.ws:spring-ws-security:4.1.1") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk18on")
    }
    implementation("com.microsoft.azure:applicationinsights-web:3.7.3")

    api("software.amazon.awssdk:s3")
    implementation("wsdl4j:wsdl4j:1.6.3")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.5") {
        exclude(group = "com.sun.xml.bind", module = "jaxb-core")
    }
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")

    xjcTool("com.sun.xml.bind:jaxb-xjc:4.0.6")
    xjcTool("com.sun.xml.bind:jaxb-impl:4.0.5")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.19.2")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.3.1")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")

    testImplementation("org.springframework.ws:spring-ws-test:4.1.1")
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
