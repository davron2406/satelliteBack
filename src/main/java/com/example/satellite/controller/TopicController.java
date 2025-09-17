package com.example.satellite.controller;


import com.example.satellite.entity.Topic;
import com.example.satellite.payload.OptionDTO;
import com.example.satellite.repository.TopicRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api") @CrossOrigin
public class TopicController {
    private final TopicRepository repo;
    public TopicController(TopicRepository repo){ this.repo = repo; }

    @PreAuthorize("hasAnyAuthority('GET_SUBJECT', 'GET_ALL_SUBJECTS')")
    @GetMapping("/subjects/{subjectId}/topics")
    public List<OptionDTO> list(@PathVariable UUID subjectId, @RequestParam(required=false) String q){
        List<Topic> items = (q==null || q.isBlank())
                ? repo.findBySubjectIdAndActiveTrueOrderByNameAsc(subjectId)
                : repo.findBySubjectIdAndNameContainingIgnoreCaseAndActiveTrueOrderByNameAsc(subjectId, q.trim());
        return items.stream().map(t -> new OptionDTO(t.getId(), t.getName())).toList();
    }
}


