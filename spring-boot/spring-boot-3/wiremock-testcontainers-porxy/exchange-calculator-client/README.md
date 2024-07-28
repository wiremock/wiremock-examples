# Using Wiremock as Testcontainers Proxy

### 1. Overview
In this tutorial, we'll use WireMock and Testcontainers to test the integration between two Spring Boot applications. Firstly, we'll use Testcontainers' _DockerComposeContainer_ to spin up a Docker container with a service exposing a REST API. 

**Then, we'll use WireMock to proxy this service, injecting delays or faults for specific requests.** This will allow us to validate the contract between the services and ensure the client application handles unexpected responses correctly.

### 2. Setting Up _DockerComposeContainer_

 Let's start by importing the [_testcontainers_](https://mvnrepository.com/artifact/org.testcontainers/testcontainers) dependency, and the [_junit-jupiter_](https://mvnrepository.com/artifact/org.testcontainers/junit-jupiter) extension:
<details>
<summary>Gradle</summary>

```gradle
testImplementation "org.testcontainers:testcontainers:${testcontainersVersion}"
testImplementation "org.testcontainers:junit-jupiter:${testcontainersVersion}"
```
</details>
<details>
<summary>Maven</summary>

```xml
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers</artifactId>
  <version>${testcontainers.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>${testcontainers.version}</version>
  <scope>test</scope>
</dependency>
```
</details>

Now, let's use _DockerComposeContainer_ to launch a Docker container based on a Docker Compose file. The service will expose a REST API, allowing us to wait for a specific endpoint to return a "200" status code to confirm that the container is ready to accept traffic.

```java
@Testcontainers
class IntegrationTest {

  @Container
  static DockerComposeContainer<?> env = new DockerComposeContainer<>(
          new File("src/test/resources/compose-test.yml"))
      .withExposedService("conversion-rates-api", 8080, 
          Wait.forHttp("/currencies").forStatusCode(200))
      .withLocalCompose(true);
  
}
```

### 3. Testing the Ideal Scenario

With this configuration, Testcontainers will start the container and bind its 8080 port to a random port one form our machine. So, in order to write our first test, we only need to find out the port where the REST API is exposed:
```java
static String testcontainerUrl() {
  return "http://localhost:" + env.getServicePort("conversion-rates-api", 8080);
}
```
Now, let's use this _URL_ to when creating the tested class, and validate the positive outcome:

```java
@Test
void shouldReturnOkForTheHappyFlow() {
  var exchange = new ExchangeCalculator(testcontainersUrl());
  
  var response = exchange.toEuro(100.00, "USD");

  assertThat(exchange.toEuro(100.00, "USD"))
    .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
    .containsExactly(HttpStatus.OK, "Exchanging 100.0 USD at a rate of 0.92 will give you 92.0 EUR");
}
```
As we can see, Testcontainers' DockerComposeContainer enables us to test the _ExchangeCalculator_ class and  verify the interaction between our services.

### 4. Using WireMock as a Proxy

We have successfully tested the client's happy flow. However, it's crucial to test various scenarios, including network failures, latency, and timeouts.

To accomplish this, we can use [_wiremock-standalone_](https://mvnrepository.com/artifact/org.wiremock/wiremock-standalone) and configure it as a proxy. Let's add this dependency to our project:
<details>
<summary>Gradle</summary>

```gradle
testImplementation "org.wiremock:wiremock-standalone:${wiremockVersion}"
```
</details>
<details>
<summary>Maven</summary>

```xml
<dependency>
  <groupId>org.wiremock</groupId>
  <artifactId>wiremock-standalone</artifactId>
  <version>${wiremock.version}</version>
  <scope>test</scope>
</dependency>
```
</details>

Now, let's add the _@WireMockTest_ extension and configure it to start the WireMock server on a specified port:

```java
@Testcontainers
@WireMockTest(httpPort = IntegrationTest.WIREMOCK_PORT)
class IntegrationTest {

  static final int WIREMOCK_PORT = 1234;

  // ...
}
```
Next, we'll update our test by instantiating the _ExchangeCalculator_ object with the WireMock URL. **We'll also configure WireMock to stub the requests for fetching currencies and proxy the calls to the underlying service provided through Testcontainers**:
```java
private final ExchangeCalculator exchange = new ExchangeCalculator(
    "http://localhost:" + WIREMOCK_PORT);

@Test
void shouldReturnOkForTheHappyFlow() {
  stubFor(get(urlMatching("/currencies/.*"))
    .willReturn(aResponse()
      .proxiedFrom(testcontainerUrl())));

  var response = exchange.toEuro(100.00, "USD");

  assertThat(response)
    .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
    .containsExactly(HttpStatus.OK, "Exchanging 100.0 USD at a rate of 0.92 will give you 92.0 EUR");
}
```

### 5. Injecting Failures and Delays

**The advantage of this setup is that we can use the proxy to inject failures and artificial delays into the proxied server's responses.**

For instance, we can deliberately return an incorrect response for a specific currency. Let's set up a test to redirect all API calls, except for those requesting the conversion rate for "GBP":

```java
@Test
void shouldReturnServerErrorWhenRequestFails() {
  stubFor(get(urlMatching("/currencies/.*"))
    .willReturn(aResponse()
      .proxiedFrom(testcontainerUrl())));
  
  stubFor(get(urlMatching("/currencies/GBP"))
    .willReturn(aResponse()
      .withBody("Wrong response, definitely not a valid response!")));

  var nokResponse = exchange.toEuro(100.00, "GBP");

  assertThat(nokResponse)
      .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
      .containsExactly(HttpStatus.INTERNAL_SERVER_ERROR, "Ooops! There was an error oun our side!");
}
```

Additionally, let's add a test to ensure the client application times out if the response takes longer than two seconds. We can achieve this by introducing a three-second delay before returning the proxied answer:

```java
@Test
void shouldReturnGatewayTimeoutWhenRequestIsTooSlow() {
  stubFor(get(urlMatching("/currencies/.*"))
    .willReturn(aResponse()
      .proxiedFrom(testcontainerUrl())));

  stubFor(get(urlMatching("/currencies/RON"))
    .willReturn(aResponse()
      .withFixedDelay(3_000)
      .withBody("Wrong response, definitely not a number!")));

  var slowResponse = exchange.toEuro(100.00, "RON");

  assertThat(slowResponse.getStatusCode())
      .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
}
```

### 6. Conclusion

In this example, we learned how to use _Testcontainers_ to spin up containers from a Docker Compose file and integrate them into our tests. We then used _WireMock_ to proxy the calls to these containers, allowing us to inject failures and artificial delays. This enabled us to test a wide variety of scenarios.

