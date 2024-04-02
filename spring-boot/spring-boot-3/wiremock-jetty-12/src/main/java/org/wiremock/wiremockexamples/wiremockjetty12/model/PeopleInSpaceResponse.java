package org.wiremock.wiremockexamples.wiremockjetty12.model;

import java.util.List;

public record PeopleInSpaceResponse(String message, List<PersonInSpace> people, int number) {}
