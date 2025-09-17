package com.example.satellite.controller;



 // adapt to your project

import com.example.satellite.entity.User;
import com.example.satellite.payload.*;
import com.example.satellite.service.SchoolClassService;
import com.example.satellite.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/classes")
public class SchoolClassController {

    private final SchoolClassService service;
    private final UserService userService;

    public SchoolClassController(SchoolClassService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    // ---------- LIST ----------
    @PreAuthorize("hasAnyAuthority('GET_ALL_CLASSES', 'GET_MY_CLASSES')")
    // Admin → all classes
    @GetMapping
    public ResponseEntity<List<SchoolClassResponse>> listAll() {

        return ResponseEntity.ok(service.listAll());

    }


    // Teacher → only their classes
    @PreAuthorize("hasAnyAuthority('GET_MY_CLASSES')")
    @GetMapping("/teacher")
    public ResponseEntity<List<SchoolClassResponse>> listMineAsTeacher() {
        User me = userService.requireUser();
        if (!me.getRole().getName().equals("TEACHER") && !me.getRole().getName().equals("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return me.getRole().getName().equals("ADMIN") ? ResponseEntity.ok(service.listAll())
                : ResponseEntity.ok(service.listForTeacher(me.getId()));
    }

    // ---------- CRUD ----------
    @PreAuthorize("hasAnyAuthority('ADD_CLASS')")
    @PostMapping
    public ResponseEntity<?> create(@Validated @RequestBody CreateClassRequest req) {
        User me = userService.requireUser();
        if (!me.getRole().getName().equals("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            var resp = service.create(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
        }
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody UpdateClassRequest req) {
//        User me = userService.requireUser();
//        if (!me.getRole().getName().equals("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//
//        try {
//            var resp = service.update(id, req);
//            return ResponseEntity.ok(resp);
//        } catch (NoSuchElementException nse) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", nse.getMessage()));
//        } catch (DataIntegrityViolationException e) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
//        } catch (IllegalArgumentException iae) {
//            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
//        }
//    }

    @PreAuthorize("hasAnyAuthority('GET_CLASS', 'GET_ALL_CLASSES')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable UUID id) {
        User me = userService.requireUser();
        if (!me.getRole().getName().equals("ADMIN") && !me.getRole().getName().equals("TEACHER")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            return ResponseEntity.ok(service.getOne(id));
        } catch (NoSuchElementException nse) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", nse.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('DELETE_CLASS')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        User me = userService.requireUser();
        if (!me.getRole().getName().equals("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- SEARCH TEACHERS/STUDENTS ----------
    // Matches your frontend: GET /api/users?role=TEACHER&q=...&limit=50
    @PreAuthorize("hasAnyAuthority('ADD_CLASS')")
    @GetMapping(path = "/users")
    public ResponseEntity<List<UserSummary>> searchUsers(@RequestParam("role") String role,
                                                         @RequestParam(value = "q", required = false) String q,
                                                         @RequestParam(value = "limit", defaultValue = "50") int limit) {

        String r = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (Objects.equals(r, "TEACHER")) {
            return ResponseEntity.ok(service.searchTeachers(q, limit));
        } else if (Objects.equals(r, "STUDENT")) {
            return ResponseEntity.ok(service.searchStudents(q, limit));
        }
        return ResponseEntity.badRequest().build();
    }

    @PreAuthorize("hasAnyAuthority('GET_ALL_CLASSES')")
    @GetMapping("/my")
    public List<OptionDTO> my(@RequestParam(required = false) String name){

        User user = userService.requireUser();
        boolean isAdmin = user.getRole().getName().equals("ADMIN");


        UUID teacherId = null;
        if (!isAdmin) {
            teacherId = user.getId();
        }

        return service.myClasses(teacherId, isAdmin, name);
    }

    @PreAuthorize("hasAnyAuthority('GET_MY_CLASSES', 'GET_ALL_CLASSES')")
    @GetMapping("/templates/results/{classId}/{templateId}")
    public ResponseEntity<List<StudentResultView>> getClassTemplateResults(
            @PathVariable UUID classId,
            @PathVariable UUID templateId) {

        List<StudentResultView> data = service.getClassResults(classId, templateId);
        return ResponseEntity.ok(data);
    }
}
