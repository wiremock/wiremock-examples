package org.wiremock.wiremockexamples.wiremockspringboot;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PeopleInSpaceService {
    
    private final OpenNotifyPeopleInSpaceClient openNotifyClient;
    
    public PeopleInSpaceService(OpenNotifyPeopleInSpaceClient openNotifyClient) {
        this.openNotifyClient = openNotifyClient;
    }
    
    public List<String> getPeopleInSpace() {
        var response = openNotifyClient.getPeopleInSpace();
        return response.people().stream()
                .map(person -> person.name() + " (" + person.craft() + ")" )
                .toList();
    }
}
