package org.wiremock.wiremockexamples.wiremockjetty12;

import org.springframework.web.service.annotation.GetExchange;
import org.wiremock.wiremockexamples.wiremockjetty12.model.PeopleInSpaceResponse;

public interface OpenNotifyPeopleInSpaceClient {
    @GetExchange("/astros.json")
    PeopleInSpaceResponse getPeopleInSpace();
}
