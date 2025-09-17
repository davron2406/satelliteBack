package com.example.satellite.controller;


import com.example.satellite.entity.Subject;
import com.example.satellite.payload.OptionDTO;
import com.example.satellite.repository.SubjectRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController @RequestMapping("/api/subjects")
public class SubjectController {
    private final SubjectRepository repo;
    public SubjectController(SubjectRepository repo){ this.repo = repo; }

    @PreAuthorize("hasAnyAuthority('GET_ALL_SUBJECTS', 'GET_SUBJECT')")
    @GetMapping
    public List<OptionDTO> list(@RequestParam(required=false) String q){
        List<Subject> items = (q==null || q.isBlank())
                ? repo.findAllByActiveTrueOrderByNameAsc()
                : repo.findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(q.trim());
        return items.stream().map(s -> new OptionDTO(s.getId(), s.getName())).toList();
    }
}


