package org.wiremock.wiremockexamples.wiremockjetty12;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class PeopleInSpaceConfig {
    @Bean
    OpenNotifyPeopleInSpaceClient openNotifyClient(WebClient.Builder builder,
                                                   @Value("${open-notify-client.base-url}") String url) {
        WebClient webClient = builder
                .baseUrl(url)
                .build();
        
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
        
        return httpServiceProxyFactory.createClient(OpenNotifyPeopleInSpaceClient.class);
    }
}
