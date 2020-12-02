# crime-portal-gateway

This project delivers a SOAP interface for the delivery of case lists from Libra. It will transform those feeds and place them on to a messaging infrastructure for consumption by other services. In the first place, this will be court-case-matcher. 

https://github.com/ministryofjustice/court-case-matcher

# Instructions

Run the application as a Spring Boot app.

```./gradlew bootRun```

# Environment variables


| Syntax           | Description | Example |
| ---------------- | ----------- |---------
| soap-default-uri | The WS endpoint to call | http://localhost:8080/crime-portal-gateway/ws|
| keystore-password | Password used for the JKS | changeit |
| trusted-cert-alias-name | Alias for the trusted certicate in the JKS | client-public |
| private-key-alias-name | Password used for the JKS | server |

# Health / Ping endpoints

The application is configured with a Spring health endpoint at the following location. It is customised to provide a status of the SQS queue.

```http://localhost:8080/health/```

There is also a "ping" endpoint available at

```http://localhost:8080/health/ping```


# WSDL

The generated WSDL will be available at the following location

```http://localhost:8080/crime-portal-gateway/ws/ExternalDocumentRequest.wsdl```

# Testing - CURL 

The application requires an Amazon SQS queue to be configured. It is possible to run the application with the Spring "local" profile. This can be achieved by 

1. Clone localstack repo (https://github.com/localstack/localstack)
2. Start localstack.

```cd localstack```
   
```SERVICES=sqs PORT_WEB_UI=9080 docker-compose up```
3. Create the SQS queue. This depends on the AWS CLI.

```aws --endpoint-url http://localhost:4566 sqs create-queue --queue-name crime-portal-gateway-queue```

4. Start the application

```SPRING_PROFILES_ACTIVE=local SQS_QUEUE_NAME=crime-portal-gateway-queue AWS_SECRET_ACCESS_KEY=none AWS_ACCESS_KEY=none ./gradlew bootRun```

5. It is possible to execute the following command from the project root to call the SOAP endpoint.

```curl --header "content-type: application/soap+xml" -d @src/test/resources/soap/sample-request.xml http://localhost:8080/crime-portal-gateway/ws```

This command should submit the message to the local queue and will report back the message ID. The message can be retrieved from the queue with the AWS CLI with a command with this form. Note that the queue URL may well be the same but will have been reported back from the create-queue command in part 3.

```aws --endpoint-url http://localhost:4566 sqs receive-message --queue-url http://localhost:4566/000000000000/crime-portal-gateway-queue```

# Testing - SOAP-UI

There is a SOAP-UI project in src/test/resources/soap-ui/single-case-simple.xml. Import this project to SOAP-UI and run it in the normal way.


