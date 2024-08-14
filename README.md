# wiremock-examples

A repository of different examples of wiremock use in different frameworks

## Spring Boot

### Wiremock Example - Spring Boot 3.x Using WireMock with the jetty 12 library

[This example](/spring-boot/spring-boot-3/wiremock-jetty-12/README.md) demonstrates how to use WireMock with Spring
Boot 3.x using the WireMock jetty 12 library.

### Using wiremock-spring-boot

[This example](/spring-boot/spring-boot-3/wiremock-spring-boot/README.md) demonstrates how to use WireMock with Spring
Boot 3.x using the [`wiremock-spring-boot`](https://github.com/maciejwalkowiak/wiremock-spring-boot) project
from [Maciej Walkowiak](https://github.com/maciejwalkowiak)

###  Using Wiremock as Testcontainers Proxy

[This example](/spring-boot/spring-boot-3/wiremock-testcontainers-proxy/exchange-calculator-client/README.md) demonstrates how to set up WireMock as proxy for a service provisioned through Testcontainers and Docker Compose. This enables us to inject failures and artificial delays into the proxied service responses and test a wide range of scenarios.