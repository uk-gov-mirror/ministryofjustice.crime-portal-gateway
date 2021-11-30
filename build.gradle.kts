plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.13"
    id("org.jetbrains.kotlin.plugin.spring") version "1.5.31"
    id("org.unbroken-dome.xjc") version "2.0.0"
    id("org.owasp.dependencycheck") version "6.3.1"
}

val jaxbVersion by extra { "2.3.1" }
val junitJupiterVersion by extra { "5.4.2" }

dependencyCheck {
    suppressionFiles.add("cpg-suppressions.xml")
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.ws:spring-ws-security") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
            .because("OWASP found security Issues")
        exclude(group = "org.cryptacular", module = "cryptacular")
            .because("OWASP found security Issues")
        exclude(group = "org.apache.santuario", module = "xmlsec")
            .because("OWASP found security Issues")
        implementation("org.apache.santuario:xmlsec:2.2.3")
    }
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("com.microsoft.azure:applicationinsights-spring-boot-starter")
    implementation("com.amazonaws:aws-java-sdk-sqs:1.11.1022")
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.1022")

    implementation("wsdl4j:wsdl4j")
    implementation("javax.xml.bind:jaxb-api:$jaxbVersion")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.2.5")
    runtimeOnly("javax.xml.bind:jaxb-api:$jaxbVersion")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:$jaxbVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testImplementation("org.springframework.ws:spring-ws-test")
    testImplementation("org.mockito:mockito-core")
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
