package org.wiremock.wiremockexamples.wiremockspringboot.model;

import java.util.List;

public record PeopleInSpaceResponse(String message, List<PersonInSpace> people, int number) {}
