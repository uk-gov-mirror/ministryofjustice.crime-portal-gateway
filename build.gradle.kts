kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.9.0"
    kotlin("plugin.spring") version "2.0.20"
    id("org.unbroken-dome.xjc") version "2.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

dependencyCheck {
    suppressionFiles.add("cpg-suppressions.xml")
}

val junitJupiterVersion by extra { "5.9.0" }
var awsSdkVersion = "1.12.747"

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
    implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")

    implementation("com.amazonaws:aws-java-sdk-s3:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-sns:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-sts:$awsSdkVersion")

    implementation("wsdl4j:wsdl4j:1.6.3")
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    // Spring uses 2.11.4 - using 2.12.3 breaks Spring.
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.2.5")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")

    testImplementation("org.testcontainers:localstack:1.19.6")
    testImplementation("org.testcontainers:junit-jupiter:1.19.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.ws:spring-ws-test:3.1.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("com.amazonaws:aws-java-sdk-sqs:$awsSdkVersion")
}

xjc {
    srcDirName.set("resources/xsd")
    extension.set(true)
}

sourceSets.named("main") {
    xjcBinding.srcDir("resources/xsd")
}

tasks {
    register<Copy>("copyAgentConfig") {
        description = "Copy applicationinsights.json to build.lib so App Insights config is applied correctly"
        from("applicationinsights.json")
        val output: Provider<Directory> = layout.buildDirectory.dir("libs")
        into(output)
    }
}

tasks.named("assemble") {
    dependsOn("copyAgentConfig")
}
