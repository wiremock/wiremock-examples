package org.wiremock.wiremockexamples.wiremockspringboot;

import org.springframework.web.service.annotation.GetExchange;
import org.wiremock.wiremockexamples.wiremockspringboot.model.PeopleInSpaceResponse;

public interface OpenNotifyPeopleInSpaceClient {
    @GetExchange("/astros.json")
    PeopleInSpaceResponse getPeopleInSpace();
}
