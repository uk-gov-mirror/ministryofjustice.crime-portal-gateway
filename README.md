# crime-portal-gateway

This project delivers a SOAP interface for the delivery of case lists from Libra. It will transform those feeds and place them on to a messaging infrastructure for consumption by other services. In the first place, this will be court-case-matcher. 

https://github.com/ministryofjustice/court-case-matcher

# Instructions

Run the application as a Spring Boot app.

```./gradlew bootRun```

# Testing - CURL

If the application has been started, it is possible to execute the following command from the project root to call the SOAP endpoint.

```curl --header "content-type: application/soap+xml" -d @src/test/resources/sample-soap-request.xml http://localhost:8080/ws```

# Testing - SOAP-UI

There is a SOAP-UI project in src/test/resources/soap-ui/single-case-simple.xml. Import this project to SOAP-UI and run it in the normal way.


