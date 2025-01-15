package org.wiremock.wiremockexamples.wiremockspringboot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(
                name = "open-notify-client", 
                filesUnderClasspath = "wiremock/open-notify-client", 
                baseUrlProperties = "open-notify-client.base-url" 
        )
})
@AutoConfigureMockMvc
public class PeopleInSpaceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectWireMock("open-notify-client")
    private WireMockServer wiremock;

    @Test
    public void shouldReturnCorrectNumberOfPeopleInSpace() throws Exception {
        // this loads the mapping file from resources/wiremock/open-notify-client/mappings/get-astros.json
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/people-in-space"))
                .andExpect(status().isOk());

        var response = result.andReturn().getResponse().getContentAsString();
        var people = objectMapper.readValue(response, String[].class);

        assertThat(people).hasSize(3);
        assertThat(people).containsExactlyInAnyOrder("Mark Vande Hei (ISS)", "Oleg Novitskiy (ISS)", "Pyotr Dubrov (ISS)");
    }

    @Test
    public void shouldReturnAnEmptyArrayWhenNoPeopleInSpace() throws Exception {
        wiremock.stubFor(WireMock.get(urlEqualTo("/astros.json"))
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
