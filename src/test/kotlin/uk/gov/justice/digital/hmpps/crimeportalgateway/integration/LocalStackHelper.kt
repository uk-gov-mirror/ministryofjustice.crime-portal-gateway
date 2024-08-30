package uk.gov.justice.digital.hmpps.crimeportalgateway.integration

import org.slf4j.LoggerFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.io.IOException
import java.net.ServerSocket

object LocalStackHelper {
    private val log = LoggerFactory.getLogger(this::class.java)
    val instance by lazy { startLocalstackIfNotRunning() }

    fun setLocalStackProperties(
        localStackContainer: LocalStackContainer,
        registry: DynamicPropertyRegistry,
    ) {
        registry.add("aws.localstack-endpoint-url") { localStackContainer.getEndpointOverride(LocalStackContainer.Service.SNS) }
        registry.add("aws.region-name") { localStackContainer.region }
    }

    private fun startLocalstackIfNotRunning(): LocalStackContainer? {
        if (localstackIsRunning()) {
            println("*************************************************")
            println("* LOCALSTACK IS ALREADY RUNNING, TESTS MAY FAIL *")
            println("*************************************************")
            return null
        }
        val logConsumer = Slf4jLogConsumer(log).withPrefix("localstack")
        return LocalStackContainer(
            DockerImageName.parse("localstack/localstack").withTag("3.0"),
        ).apply {
            withServices(LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS, LocalStackContainer.Service.S3)
            withEnv("HOSTNAME_EXTERNAL", "localhost")
            withEnv("DEFAULT_REGION", "eu-west-2")
            waitingFor(
                Wait.forLogMessage(".*Ready.*", 1),
            )
            start()
            followOutput(logConsumer)
        }
    }

    private fun localstackIsRunning(): Boolean =
        try {
            val serverSocket = ServerSocket(4566)
            serverSocket.localPort == 0
        } catch (e: IOException) {
            true
        }
}
