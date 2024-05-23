# Wiremock Example - Spring Boot 3.x Using WireMock with the jetty 12 library

This example demonstrates how to use WireMock with Spring Boot 3.x using the WireMock jetty 12 library.

The jetty 12 library can be introduced as a dependency in the `pom.xml` file:

```xml
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-jetty12</artifactId>
    <version>3.6.0</version>
</dependency>
```
or in the `build.gradle` file:

```groovy
testImplementation 'org.wiremock:wiremock-jetty12:3.6.0'
```

WireMock can then be used in your tests as follows:

```java 
@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 8181)
public class PeopleInSpaceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnCorrectNumberOfPeopleInSpace() throws Exception {
        // the mapping for this request is loaded from the resources folder
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/people-in-space"))
                .andExpect(status().isOk());

        var response = result.andReturn().getResponse().getContentAsString();
        var people = objectMapper.readValue(response, String[].class);

        assertThat(people).hasSize(3);
        assertThat(people).containsExactlyInAnyOrder("Mark Vande Hei (ISS)", "Oleg Novitskiy (ISS)", "Pyotr Dubrov (ISS)");
    }

    @Test
    public void shouldReturnAnEmptyArrayWhenNoPeopleInSpace() throws Exception {
        stubFor(WireMock.get(urlEqualTo("/astros.json"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "people": [],
                                    "number": 0,
                                    "message": "success"
                                }
                                """)));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/people-in-space"))
                .andExpect(status().isOk());

        var response = result.andReturn().getResponse().getContentAsString();
        var people = objectMapper.readValue(response, String[].class);

        assertThat(people).hasSize(0);
    }

}
```