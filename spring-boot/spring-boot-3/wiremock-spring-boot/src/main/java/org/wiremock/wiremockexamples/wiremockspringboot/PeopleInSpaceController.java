package org.wiremock.wiremockexamples.wiremockspringboot;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/people-in-space")
public class PeopleInSpaceController {
    
    private final PeopleInSpaceService peopleInSpaceService;

    public PeopleInSpaceController(PeopleInSpaceService peopleInSpaceService) {
        this.peopleInSpaceService = peopleInSpaceService;
    }

    @GetMapping()
    List<String> getPeopleInSpace() {
        return peopleInSpaceService.getPeopleInSpace();
    }
}
