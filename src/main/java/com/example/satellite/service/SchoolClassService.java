package com.example.satellite.service;


import com.example.satellite.entity.Role;
import com.example.satellite.entity.SchoolClass;
import com.example.satellite.entity.User;
import com.example.satellite.payload.*;
import com.example.satellite.repository.RoleRepository;
import com.example.satellite.repository.SchoolClassRepository;
import com.example.satellite.repository.UserRepository;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchoolClassService {

    private final SchoolClassRepository classRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final UserService userService;

    public SchoolClassService(SchoolClassRepository classRepo, UserRepository userRepo, RoleRepository roleRepository, SchoolClassRepository schoolClassRepository, UserService userService) {
        this.classRepo = classRepo;
        this.userRepo = userRepo;
        this.roleRepository = roleRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.userService = userService;
    }

    // ---------- helpers ----------
    private static UserSummary toSummary(User u) {
        return new UserSummary(u.getId(),
                Optional.ofNullable(u.getFirstName()).orElseGet(() ->
                        (u.getFirstName() != null && u.getLastName() != null)
                                ? (u.getFirstName() + " " + u.getLastName())
                                : Optional.ofNullable(u.getUsername()).orElse(u.getEmail())),
                u.getEmail(),
                u.getRole() != null ? u.getRole().getName() : null);
    }

    private static SchoolClassResponse toResponse(SchoolClass c) {
        var teachers = Optional.ofNullable(c.getTeachers()).orElseGet(Set::of)
                .stream().map(SchoolClassService::toSummary).toList();
        int studentCount = Optional.ofNullable(c.getStudents()).map(Set::size).orElse(0);
        return new SchoolClassResponse(c.getId(), c.getName(), teachers, studentCount);
    }

    private void assertUniqueName(String name, UUID ignoreId) {
        var existing = classRepo.findByNameIgnoreCase(name.trim());
        if (existing.isPresent() && (ignoreId == null || !existing.get().getId().equals(ignoreId))) {
            throw new DataIntegrityViolationException("Class name must be unique");
        }
    }

    private Set<User> loadUsers(Set<UUID> ids, Role requiredRole) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        var list = userRepo.findByIdIn(ids);
        // filter by role (safety)
        return list.stream()
                .filter(u -> u.getRole() == requiredRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // ---------- class CRUD ----------
    @Transactional
    public SchoolClassResponse create(CreateClassRequest req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        assertUniqueName(req.name, null);

        var teachers = loadUsers(req.teacherIds, roleRepository.findByName("TEACHER"));
        if (teachers.isEmpty()) throw new IllegalArgumentException("At least one teacher is required");

        var students = loadUsers(Optional.ofNullable(req.studentIds).orElseGet(Set::of), roleRepository.findByName("STUDENT"));

        var c = new SchoolClass();
        c.setName(req.name.trim());
        c.setTeachers(teachers);
        c.setStudents(students);

        c = classRepo.save(c);
        return toResponse(c);
    }

//    @Transactional
//    public SchoolClassResponse update(UUID id, UpdateClassRequest req) {
//        var c = classRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Class not found"));
//
//        if (req.name != null && !req.name.isBlank() && !req.name.trim().equalsIgnoreCase(c.getName())) {
//            assertUniqueName(req.name, id);
//            c.setName(req.name.trim());
//        }
//
//        if (req.teacherIds != null) {
//            var teachers = loadUsers(req.teacherIds, Role.TEACHER);
//            if (teachers.isEmpty()) throw new IllegalArgumentException("At least one teacher is required");
//            c.setTeachers(teachers);
//        }
//
//        if (req.studentIds != null) {
//            var students = loadUsers(req.studentIds, Role.STUDENT);
//            c.setStudents(students);
//        }
//
//        c = classRepo.save(c);
//        return toResponse(c);
//    }

    @Transactional
    public void delete(UUID id) {
        classRepo.deleteById(id);
    }

    @Transactional
    public SchoolClassResponse getOne(UUID id) {
        return classRepo.findById(id).map(SchoolClassService::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Class not found"));
    }

    @Transactional
    public List<SchoolClassResponse> listAll() {
        User user = userService.requireUser();
        if(user.getRole().getName().equals("ADMIN")) {
            return classRepo.findAll().stream().map(SchoolClassService::toResponse).toList();
        }
        if(user.getRole().getName().equals("TEACHER")) {
            return classRepo.findByTeachers_Id(user.getId()).stream().map(SchoolClassService::toResponse).toList();
        }
        else{
            return null;
        }
    }

    @Transactional
    public List<SchoolClassResponse> listForTeacher(UUID teacherId) {
        return classRepo.findByTeachers_Id(teacherId).stream().map(SchoolClassService::toResponse).toList();
    }

    // ---------- user search ----------
    @Transactional
    public List<UserSummary> searchTeachers(String q, int limit) {
        var list = userRepo.searchByRole(roleRepository.findByName("TEACHER"), q, PageRequest.of(0, Math.max(1, Math.min(limit, 20))));
        return list.stream().map(SchoolClassService::toSummary).toList();
    }

    @Transactional
    public List<UserSummary> searchStudents(String q, int limit) {
        var list = userRepo.searchByRole(roleRepository.findByName("STUDENT"), q, PageRequest.of(0, Math.max(1, Math.min(limit, 20))));
        return list.stream().map(SchoolClassService::toSummary).toList();
    }

    @Transactional
    public List<OptionDTO> myClasses(UUID teacherIdOrNull, boolean isAdmin, @Nullable String name){
        String q = (name == null || name.isBlank()) ? null : name.trim();

        List<OptionInterface> rows = isAdmin
                ? schoolClassRepository.findAllByName(q)
                : schoolClassRepository.findByTeacherAndName(Objects.requireNonNull(teacherIdOrNull), q);

        return rows.stream().map(p -> new OptionDTO(p.getId(), p.getName())).toList();
    }

    @Transactional
    public List<StudentResultView> getClassResults(UUID classId, UUID templateId) {
        return schoolClassRepository.findClassStudentResults(classId, templateId);
    }
}

