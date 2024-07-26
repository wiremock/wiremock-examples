package etr.learning.conversionrates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
public class ConversionRatesApp {

  public static void main(String[] args) {
    SpringApplication.run(ConversionRatesApp.class, args);
  }

  @RestController
  static class RestApi {

    private final Map<String, Double> conversionRates = Map.of(
        "EUR", 1.0,
        "USD", 0.92,
        "GBP", 1.19,
        "JPY", 0.0058,
        "CAD", 0.67,
        "RON", 0.2
    );

    @GetMapping("/currencies/{currency}")
    ResponseEntity<Double> get(@PathVariable String currency) {
      if (conversionRates.containsKey(currency)) {
        return ResponseEntity.ok(conversionRates.get(currency));
      }
      return ResponseEntity.notFound().build();
    }

    @GetMapping("/currencies")
    Map<String, Double> getAll() {
      return conversionRates;
    }
  }

}
