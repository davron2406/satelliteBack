package com.example.satellite.controller;


import com.example.satellite.entity.PracticeTemplate;
import com.example.satellite.entity.User;
import com.example.satellite.payload.ApiResponse;
import com.example.satellite.payload.CreateTemplateRequest;
import com.example.satellite.payload.OptionDTO;
import com.example.satellite.payload.StartTestResponse;
import com.example.satellite.repository.PracticeTemplateRepository;
import com.example.satellite.service.PracticeTemplateService;
import com.example.satellite.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/templates") @CrossOrigin
public class PracticeTemplateController {

    private final PracticeTemplateService service;
    private final PracticeTemplateRepository repo;
    private final UserService current;

    public PracticeTemplateController(PracticeTemplateService s, UserService c, PracticeTemplateRepository r){
        this.service = s; this.current = c; this.repo = r;
    }

    // Create template (TEACHER or ADMIN)

    @PreAuthorize("hasAnyAuthority('ADD_PRACTICE_TEST')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UUID> create(@RequestBody CreateTemplateRequest req){
        return ResponseEntity.ok(service.createTemplate(req));
    }

    @PreAuthorize("hasAnyAuthority('GET_ALL_PRACTICE_TESTS')")
    @GetMapping
    public Page<OptionDTO> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return repo.findAll(pageable).map(this::toOption);
    }


    private OptionDTO toOption(PracticeTemplate t) {
        // If OOptionDto takes Long id:
        // return new OOptionDto(t.getId(), t.getTitle());
    
        // If OOptionDto takes String id (UUIDs etc.), use String.valueOf:
        return new OptionDTO(t.getId(), t.getTitle());
    }

    // Start test for ME (must be STUDENT)
    @PreAuthorize("hasAuthority('START_PRACTICE_TEST')")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse> startForMe(@RequestParam UUID templateId){
        User me = current.requireUser();
        if (me.getRole()==null )
            return ResponseEntity.status(403).build();
        StartTestResponse startTestResponse = service.startForUser(templateId, me.getId());
        if(startTestResponse.testId ==null){
            return ResponseEntity.ok().body(new ApiResponse("You have already solved this test", false, startTestResponse));
        }

        return ResponseEntity.ok(new ApiResponse("Success", true, startTestResponse));
    }

    @PreAuthorize("hasAnyAuthority('GET_PRACTICE_TEST', 'GET_ALL_PRACTICE_TESTS')")
    @GetMapping("/search")
    public List<OptionDTO> search(@RequestParam String name) {
        return service.searchByName(name);
    }


    @PreAuthorize("hasAnyAuthority('DELETE_PRACTICE_TEST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable UUID id) {
        try {
            service.deleteTemplate(id);
            return ResponseEntity.ok().body("Template deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete template: " + e.getMessage());
        }
    }

}

