package etr.learning.moneyexchange;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import static java.lang.Double.parseDouble;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

@SpringBootApplication
class MoneyExchangeApp {

  public static void main(String[] args) {
    SpringApplication.run(MoneyExchangeApp.class, args);
  }

  @RestController
  static class ExchangeCalculator {

    private final HttpClient client;
    private final String baseUrl;

    ExchangeCalculator(@Value("${conversion-rates-api.base-url}") String baseUrl) {
      this.client = HttpClient.newHttpClient();
      this.baseUrl = baseUrl;
    }

    @GetMapping("/exchanges")
    ResponseEntity<String> toEuro(@RequestParam Double value, @RequestParam String currency) {
      try {
        return ResponseEntity.ok(fetchRateAndExchange(value, currency));
      } catch (HttpTimeoutException timeout) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
            .body("Ooops! There was an error oun our side!");
      } catch (Exception e) {
        return ResponseEntity.internalServerError()
            .body("Ooops! There was an error oun our side!");
      }
    }

    private String fetchRateAndExchange(Double value, String currency)
        throws IOException, InterruptedException {
      var rate = fetchRate(currency);
      var eurValue = value * rate;
      return "Exchanging %s %s at a rate of %s will give you %s EUR"
          .formatted(value, currency, rate, eurValue);
    }

    private double fetchRate(String currency) throws IOException, InterruptedException {
      var request = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/currencies/" + currency))
          .timeout(Duration.ofSeconds(2))
          .GET()
          .build();

      var response = client.send(request, ofString());
      return parseDouble(response.body());
    }
  }

}
