package etr.learning.moneyexchange;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import etr.learning.moneyexchange.MoneyExchangeApp.ExchangeCalculator;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@WireMockTest(httpPort = IntegrationTest.WIREMOCK_PORT)
class IntegrationTest {

  static final int WIREMOCK_PORT = 1234;
  private final ExchangeCalculator exchange = new ExchangeCalculator(
      "http://localhost:" + WIREMOCK_PORT);

  @Container
  static DockerComposeContainer<?> env = new DockerComposeContainer<>(
      new File("src/test/resources/compose-test.yml"))
      .withExposedService("conversion-rates-api", 8080,
          Wait.forHttp("/currencies").forStatusCode(200))
      .withLocalCompose(true);

  @Test
  void shouldReturnOkForTheHappyFlow() {
    // given
    stubFor(get(urlMatching("/currencies/.*"))
        .willReturn(aResponse()
            .proxiedFrom(testcontainerUrl())));
    // when
    var okResponse = exchange.toEuro(100.00, "USD");

    //then
    assertThat(okResponse)
        .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
        .containsExactly(HttpStatus.OK,
            "Exchanging 100.0 USD at a rate of 0.92 will give you 92.0 EUR");
  }

  @Test
  void shouldReturnServerErrorWhenRequestFails() {
    // given
    stubFor(get(urlMatching("/currencies/.*"))
        .willReturn(aResponse()
            .proxiedFrom(testcontainerUrl())));

    stubFor(get(urlMatching("/currencies/GBP"))
        .willReturn(aResponse()
            .withBody("Wrong response, definitely not a valid response!")));
    // when
    var nokResponse = exchange.toEuro(100.00, "GBP");

    // then
    assertThat(nokResponse)
        .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
        .containsExactly(HttpStatus.INTERNAL_SERVER_ERROR,
            "Ooops! There was an error oun our side!");
  }

  @Test
  void shouldReturnGatewayTimeoutWhenRequestIsTooSlow() {
    // given
    stubFor(get(urlMatching("/currencies/.*"))
        .willReturn(aResponse()
            .proxiedFrom(testcontainerUrl())));

    stubFor(get(urlMatching("/currencies/RON"))
        .willReturn(aResponse()
            .withFixedDelay(3_000)
            .withBody("Wrong response, definitely not a number!")));
    // when
    var slowResponse = exchange.toEuro(100.00, "RON");

    // then
    assertThat(slowResponse.getStatusCode())
        .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
  }

  static String testcontainerUrl() {
    return "http://localhost:" + env.getServicePort("conversion-rates-api", 8080);
  }

}
