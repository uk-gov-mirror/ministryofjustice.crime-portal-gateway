import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.4"
    kotlin("plugin.spring") version "2.0.20"
    id("org.unbroken-dome.xjc") version "2.0.0"
    kotlin("jvm") version "2.0.20"
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

val junitJupiterVersion by extra { "5.9.0" }
var awsSdkVersion = "1.12.543"
val springBootVersion = "3.0.2"

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.ws:spring-ws-security:4.0.1") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
            .because("OWASP found security Issues")
        exclude(group = "org.cryptacular", module = "cryptacular")
            .because("OWASP found security Issues")
        exclude(group = "org.apache.santuario", module = "xmlsec")
            .because("OWASP found security Issues")
        implementation("org.apache.santuario:xmlsec:3.0.0")
    }
    implementation("org.springframework.boot:spring-boot-starter-web-services")

    implementation("com.microsoft.azure:applicationinsights-web:3.5.4")

    implementation("com.amazonaws:aws-java-sdk-s3:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-sns:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-sts:$awsSdkVersion")

    implementation("wsdl4j:wsdl4j:1.6.3")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.5")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")

    xjcTool("com.sun.xml.bind:jaxb-xjc:3.0.2")
    xjcTool("com.sun.xml.bind:jaxb-impl:3.0.2")

    // Spring uses 2.11.4 - using 2.12.3 breaks Spring.
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.2.5")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
    testImplementation("org.testcontainers:localstack:1.19.6")
    testImplementation("org.testcontainers:junit-jupiter:1.19.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.ws:spring-ws-test:3.1.3")
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("com.amazonaws:aws-java-sdk-sqs:$awsSdkVersion")
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
