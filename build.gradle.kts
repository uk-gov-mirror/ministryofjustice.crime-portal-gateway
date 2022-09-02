plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.4.2"
    kotlin("plugin.spring") version "1.7.10"
    id("org.unbroken-dome.xjc") version "2.0.0"
}

dependencyCheck {
    suppressionFiles.add("cpg-suppressions.xml")
}

val junitJupiterVersion by extra { "5.9.0" }
val awsSdkVersion = "1.12.295"
val springBootVersion = "2.7.3"

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
    implementation("org.springframework.ws:spring-ws-security:3.1.3") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
            .because("OWASP found security Issues")
        exclude(group = "org.cryptacular", module = "cryptacular")
            .because("OWASP found security Issues")
        exclude(group = "org.apache.santuario", module = "xmlsec")
            .because("OWASP found security Issues")
        implementation("org.apache.santuario:xmlsec:3.0.0")
    }
    implementation("org.springframework.boot:spring-boot-devtools:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web-services:$springBootVersion")
    implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
    implementation("com.amazonaws:aws-java-sdk-sqs:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-s3:$awsSdkVersion")

    implementation("wsdl4j:wsdl4j:1.6.3")
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.2.5")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.springframework.ws:spring-ws-test:3.1.3")
    testImplementation("org.mockito:mockito-core:4.7.0")
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
        into("$buildDir/libs")
    }
}

tasks.named("assemble") {
    dependsOn("copyAgentConfig")
}
