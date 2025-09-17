package com.example.satellite.controller;


import com.example.satellite.entity.Answer;
import com.example.satellite.entity.Question;
import com.example.satellite.entity.Subject;
import com.example.satellite.entity.Topic;
import com.example.satellite.entity.enums.Difficulty;
import com.example.satellite.payload.AnswerDTO;
import com.example.satellite.payload.AnswerOptionDTO;
import com.example.satellite.payload.QuestionDTO;
import com.example.satellite.payload.QuestionOptionDTO;
import com.example.satellite.repository.AnswerRepository;
import com.example.satellite.repository.QuestionRepository;
import com.example.satellite.repository.SubjectRepository;
import com.example.satellite.repository.TopicRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class QuestionController {

    private final QuestionRepository questionRepo;
    private final AnswerRepository answerRepo;
    private final SubjectRepository subjectRepo;
    private final TopicRepository topicRepo;
    public QuestionController(QuestionRepository qr, AnswerRepository ar, SubjectRepository s, TopicRepository t) {
        this.questionRepo = qr; this.answerRepo = ar; this.subjectRepo = s;this.topicRepo = t;
    }

    // ---------- CREATE (JSON with Base64 images for question + answers)
    @PreAuthorize("hasAnyAuthority('ADD_QUESTION')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<QuestionOptionDTO> create(@RequestBody QuestionDTO dto) {
        Question q = new Question();
        applyQuestionFields(q, dto);



        q = questionRepo.save(q);


        // answers
        if (dto.answers != null) {
            int i = 0;
            for (AnswerDTO aDto : dto.answers) {
                Answer a = new Answer();
                a.setQuestion(q);
                applyAnswerFields(a, aDto, i++);
                answerRepo.save(a);
            }
        }
        questionRepo.flush();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(loadFull(q.getId())));
    }

    // ---------- UPDATE (JSON with Base64 images)
//    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @Transactional
//    public ResponseEntity<QuestionOptionDTO> update(@PathVariable UUID id, @RequestBody QuestionDTO dto) {
//        Question q = questionRepo.findWithAnswers(id).orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));
//        applyQuestionFields(q, dto);
//
//        // Simple strategy: update by (optional) answer id; create if id null; delete the rest
//        Map<Long, Answer> existing = (q.getAnswers() == null) ? Map.of() :
//                q.getAnswers().stream().collect(Collectors.toMap(Answer::getId, x -> x));
//
//        Set<UUID> keep = new HashSet<>();
//        int pos = 0;
//        if (dto.answers != null) {
//            for (AnswerDTO aDto : dto.answers) {
//                Answer a;
//                if (aDto.id != null && existing.containsKey(aDto.id)) {
//                    a = existing.get(aDto.id);
//                } else {
//                    a = new Answer();
//                    a.setQuestion(q);
//                }
//                applyAnswerFields(a, aDto, pos++);
//                answerRepo.save(a);
//                if (a.getId() != null) keep.add(a.getId());
//            }
//        }
//        // delete removed answers
//        if (q.getAnswers() != null) {
//            for (Answer a : new ArrayList<>(q.getAnswers())) {
//                if (a.getId() != null && !keep.contains(a.getId())) {
//                    answerRepo.delete(a);
//                }
//            }
//        }
//
//        questionRepo.flush();
//        return ResponseEntity.ok(toDto(loadFull(q.getId())));
//    }

    // ---------- READ (return with data URLs)
    @PreAuthorize("hasAnyAuthority('GET_QUESTION', 'GET_ALL_QUESTIONS')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<QuestionOptionDTO> get(@PathVariable UUID id) {
        Question q = loadFull(id);
        return ResponseEntity.ok(toDto(q));
    }

    // ---------- OPTIONAL: dedicated image uploads inside SAME controller (multipart)
    @PreAuthorize("hasAnyAuthority('ADD_QUESTION')")
    @PostMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Void> uploadQuestionImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) throws Exception {
        Question q = questionRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));
        if (file == null || file.isEmpty()) {
            q.setImageBase64(null); q.setImageContentType(null); q.setImageFilename(null);
        } else {
            q.setImageBase64(Base64.getEncoder().encodeToString(file.getBytes()));
            q.setImageContentType(file.getContentType());
            q.setImageFilename(file.getOriginalFilename());
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ADD_QUESTION')")
    @PostMapping(path = "/{qid}/answers/{aid}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Void> uploadAnswerImage(@PathVariable UUID qid, @PathVariable UUID aid, @RequestParam("file") MultipartFile file) throws Exception {
        Question q = questionRepo.findById(qid).orElseThrow(() -> new EntityNotFoundException("Question not found: " + qid));
        Answer a = answerRepo.findById(aid).orElseThrow(() -> new EntityNotFoundException("Answer not found: " + aid));
        if (!Objects.equals(a.getQuestion().getId(), q.getId())) throw new EntityNotFoundException("Answer does not belong to question");
        if (file == null || file.isEmpty()) {
            a.setImageBase64(null); a.setImageContentType(null); a.setImageFilename(null);
        } else {
            a.setImageBase64(Base64.getEncoder().encodeToString(file.getBytes()));
            a.setImageContentType(file.getContentType());
            a.setImageFilename(file.getOriginalFilename());
        }
        return ResponseEntity.noContent().build();
    }

    // ---------- helpers

    private Question loadFull(UUID id) {
        return questionRepo.findWithAnswers(id).orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));
    }

    private void applyQuestionFields(Question q, QuestionDTO dto) {
         q.setText(dto.text);
         q.setDifficulty(Enum.valueOf(Difficulty.class,dto.difficulty));
         q.setHint(dto.getHint());
         q.setPoints(dto.getPoint());
         q.setSolution(dto.getSolution());

        if (!isBlank(dto.subject)) {
            Subject subject = getOrCreateSubject(dto.subject);

            if (!isBlank(dto.topic)) {
                Topic topic = getOrCreateTopic(subject, dto.topic);
                q.setTopic(topic);
            } else {
                q.setTopic(null);
            }
        } else {
            q.setTopic(null);
        }

         // if you link by ids, resolve here
        // Accept data URL or raw base64:
        String b64 = normalizeBase64(dto.imageBase64);
        q.setImageBase64(isBlank(b64) ? null : b64);
        q.setImageContentType(emptyToNull(dto.imageContentType));
        q.setImageFilename(emptyToNull(dto.imageFilename));


    }

    private void applyAnswerFields(Answer a, AnswerDTO dto, int fallbackPos) {
        a.setText(dto.text);
        if (dto.correct != null) a.setCorrect(dto.correct);
        a.setPosition((short) (dto.position != null ? dto.position : fallbackPos));

        String b64 = normalizeBase64(dto.imageBase64);
        a.setImageBase64(isBlank(b64) ? null : b64);
        a.setImageContentType(emptyToNull(dto.imageContentType));
        a.setImageFilename(emptyToNull(dto.imageFilename));
    }

    private Subject getOrCreateSubject(String raw) {
        String name = normalizeName(raw);
        return subjectRepo.findByNameIgnoreCase(name).orElseGet(() -> {
            try {
                Subject s = new Subject();
                s.setName(name);
                return subjectRepo.saveAndFlush(s);
            } catch (DataIntegrityViolationException e) {
                // another txn created it; fetch again
                return subjectRepo.findByNameIgnoreCase(name).orElseThrow();
            }
        });
    }

    private Topic getOrCreateTopic(Subject subject, String raw) {
        String name = normalizeName(raw);
        return topicRepo.findBySubjectAndNameIgnoreCase(subject, name).orElseGet(() -> {
            try {
                Topic t = new Topic();
                t.setSubject(subject);
                t.setName(name);
                return topicRepo.saveAndFlush(t);
            } catch (DataIntegrityViolationException e) {
                return topicRepo.findBySubjectAndNameIgnoreCase(subject, name).orElseThrow();
            }
        });
    }

    /* ===== small utils ===== */
    private static String normalizeBase64(String input) {
        if (input == null) return null;
        int i = input.indexOf("base64,");
        return (i >= 0) ? input.substring(i + "base64,".length()).trim() : input.trim();
    }

    private static String normalizeName(String s){
        String n = (s == null ? "" : s.trim().replaceAll("\\s+", " "));
        if (n.isBlank()) throw new IllegalArgumentException("Name is required");
        return n;
    }



    private static boolean isBlank(String s){ return s == null || s.isBlank(); }
    private static String emptyToNull(String s){ return (s == null || s.isBlank()) ? null : s; }

    private QuestionOptionDTO toDto(Question q) {
        String qImg = dataUrl(q.getImageContentType(), q.getImageBase64());
        List<AnswerOptionDTO> answers = q.getAnswers()==null ? List.of()
                : q.getAnswers().stream()
                .sorted(Comparator
                        .comparing(Answer::getPosition)
                        .thenComparing(Answer::getId))
                .map(a -> new AnswerOptionDTO(a.getId(), a.getText(), dataUrl(a.getImageContentType(), a.getImageBase64())))
                .toList();
        return new QuestionOptionDTO(q.getId(), q.getText(),q.getHint(), qImg, answers);
    }

    private static String dataUrl(String contentType, String base64) {
        if (base64 == null || base64.isBlank()) return null;
        String ct = (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        return "data:" + ct + ";base64," + base64;
    }
}
