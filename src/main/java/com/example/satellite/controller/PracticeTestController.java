package com.example.satellite.controller;


import com.example.satellite.entity.User;
import com.example.satellite.payload.SubmitPracticeTestRequest;
import com.example.satellite.payload.SubmitPracticeTestResponse;
import com.example.satellite.payload.TestContentDTO;
import com.example.satellite.repository.PracticeTestQuestionRepository;
import com.example.satellite.repository.PracticeTestRepository;
import com.example.satellite.service.PracticeTestService;
import com.example.satellite.service.QuestionService;
import com.example.satellite.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController @RequestMapping("/api/tests") @CrossOrigin
public class PracticeTestController {
    private final PracticeTestRepository testRepo;
    private final PracticeTestQuestionRepository linkRepo;
    private final QuestionService questionService;
    private final PracticeTestService practiceTestService;
    private final UserService userService;

    public PracticeTestController(PracticeTestRepository t, PracticeTestQuestionRepository l, QuestionService q, PracticeTestService practiceTestService, UserService userService){
        this.testRepo=t; this.linkRepo=l; this.questionService = q;
        this.practiceTestService = practiceTestService;
        this.userService = userService;
    }

    @PreAuthorize("hasAnyAuthority('GET_PRACTICE_TEST','GET_ALL_PRACTICE_TESTS')")
    @GetMapping("/{id}")
    public ResponseEntity<TestContentDTO> get(@PathVariable UUID id,
                                              @RequestParam(defaultValue = "true") boolean embedImages) {
        TestContentDTO test = practiceTestService.getTest(id, embedImages);

        return ResponseEntity.ok(test);
    }

    @PreAuthorize("hasAnyAuthority('START_PRACTICE_TEST')")
    @PostMapping("/{practiceTestId}/submit")
    public ResponseEntity<SubmitPracticeTestResponse> submit(
            @PathVariable UUID practiceTestId,
            @RequestBody SubmitPracticeTestRequest payload) {

        User me = userService.requireUser(); // however you get the logged-in user
        var resp = practiceTestService.submit(me.getId(), practiceTestId, payload);
        return ResponseEntity.ok(resp);
    }
}

