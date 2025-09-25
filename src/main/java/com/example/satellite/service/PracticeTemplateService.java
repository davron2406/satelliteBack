package com.example.satellite.service;


import com.example.satellite.entity.*;
import com.example.satellite.entity.enums.AttemptStatus;
import com.example.satellite.entity.enums.Difficulty;
import com.example.satellite.payload.CreateTemplateRequest;
import com.example.satellite.payload.OptionDTO;
import com.example.satellite.payload.StartTestResponse;
import com.example.satellite.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
public class PracticeTemplateService {

    private final PracticeTemplateRepository tplRepo;
    private final PracticeTemplateLineRepository lineRepo;
    private final PracticeTestRepository testRepo;
    private final PracticeTestQuestionRepository testQRepo;
    private final UserRepository userRepo;
    private final QuestionRepository questionRepo;
    private final PracticeTestResultRepository practiceTestResultRepository;
    private final PracticeTemplateRepository practiceTemplateRepository;
    private final PracticeTestRepository practiceTestRepository;
    private final TopicRepository topicRepository;

    public PracticeTemplateService(PracticeTemplateRepository tplRepo,
                                   PracticeTemplateLineRepository lineRepo,
                                   PracticeTestRepository testRepo,
                                   PracticeTestQuestionRepository testQRepo,
                                   UserRepository userRepo,
                                   QuestionRepository questionRepo, PracticeTestResultRepository practiceTestResultRepository, PracticeTemplateRepository practiceTemplateRepository, PracticeTestRepository practiceTestRepository, TopicRepository topicRepository) {
        this.tplRepo = tplRepo; this.lineRepo = lineRepo;
        this.testRepo = testRepo; this.testQRepo = testQRepo;
        this.userRepo = userRepo; this.questionRepo = questionRepo;
        this.practiceTestResultRepository = practiceTestResultRepository;
        this.practiceTemplateRepository = practiceTemplateRepository;
        this.practiceTestRepository = practiceTestRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public UUID createTemplate(CreateTemplateRequest req) {
        if (req == null || req.lines == null || req.lines.isEmpty())
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "At least one line is required");

        PracticeTemplate t = new PracticeTemplate();
        List<PracticeTemplateLine> lines = new ArrayList<>();
        for (var l : req.lines) {
            Difficulty diff = parseDiff(l.difficulty);
            int count = Math.max(0, l.count == null ? 0 : l.count);
            if (count == 0) continue;
            if (l.topicId == null )
                throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "Each line needs topicIds");

            var topic = new Topic(); topic.setId(l.topicId);
            PracticeTemplateLine line = new PracticeTemplateLine();
            line.setDifficulty(diff); line.setCount(count); line.setTopic(topic);
           lines.add(line);
        }

        t.setTitle(blank(req.title) ? "Untitled Template" : req.title.trim());
        t.setTimeLimitSec(sanitize(req.timeLimitSec));
        t.setShuffle(Boolean.TRUE.equals(req.shuffle));
        t.setAllowPartial(Boolean.TRUE.equals(req.allowPartial));
        t.setPracticeTemplateLines(lines);
        PracticeTemplate saved = tplRepo.save(t);


        return saved.getId();
    }

    /** Start a test for a given user id (must be STUDENT) from a template. */
    public StartTestResponse startForUser(UUID templateId, UUID userId) {
        PracticeTemplate tpl = tplRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Template not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        Optional<PracticeTest> byUserIdAndPracticeTemplate = practiceTestRepository.getByUserIdAndPracticeTemplateAndStatus(userId, tpl, AttemptStatus.SUBMITTED);
        if (byUserIdAndPracticeTemplate.isPresent() && byUserIdAndPracticeTemplate.get().getStatus() == AttemptStatus.SUBMITTED) {
            return new StartTestResponse();
        } else {
            var lines = tpl.getPracticeTemplateLines();

            List<Question> picked = new ArrayList<>();
            Set<UUID> used = new HashSet<>();

            for (var line : lines) {
                Topic topic = line.getTopic();
                List<Question> pool = new ArrayList<>();
                pool.addAll(questionRepo.findByTopicIdAndDifficulty(topic.getId(), line.getDifficulty()));
                pool.removeIf(q -> used.contains(q.getId()));
                Collections.shuffle(pool, ThreadLocalRandom.current());

                int need = line.getCount();
                if (!tpl.isAllowPartial() && pool.size() < need) {
                    throw new ResponseStatusException(UNPROCESSABLE_ENTITY,
                            "Not enough " + line.getDifficulty() + " questions; need " + need + ", have " + pool.size());
                }
                int take = Math.min(need, pool.size());
                for (int i = 0; i < take; i++) {
                    var q = pool.get(i);
                    if (used.add(q.getId())) picked.add(q);
                }


                if (tpl.isShuffle()) Collections.shuffle(picked, ThreadLocalRandom.current());

                PracticeTest test = new PracticeTest();
                test.setTitle(tpl.getTitle());
                test.setUser(user);
                test.setTimeLimitSec(tpl.getTimeLimitSec());
                test.setShuffle(tpl.isShuffle());
                test.setAllowPartial(tpl.isAllowPartial());
                Instant now = Instant.now();
                test.setStartedAt(now);
                test.setEndsAt(now.plusSeconds(tpl.getTimeLimitSec()));
                test.setStatus(AttemptStatus.ACTIVE);
                test.setPracticeTemplate(tpl);
                PracticeTest saved = testRepo.save(test);

                List<PracticeTestQuestion> links = new ArrayList<>(picked.size());
                for (int i = 0; i < picked.size(); i++) {
                    var link = new PracticeTestQuestion();
                    link.setTest(saved);
                    link.setQuestion(picked.get(i));
                    link.setPosition(i);
                    links.add(link);
                }
                testQRepo.saveAll(links);

                StartTestResponse out = new StartTestResponse();
                out.testId = saved.getId();
                out.timeLimitSec = saved.getTimeLimitSec();
                out.startedAt = saved.getStartedAt();
                out.endsAt = saved.getEndsAt();
                out.totalQuestions = links.size();
                return out;
            }
        }

        return new StartTestResponse();
    }


    public List<OptionDTO> searchByName(String name) {
        String q = (name == null) ? "" : name.trim();
        if (q.length() < 3) return List.of();
        return practiceTemplateRepository.findByNameLike(q).stream()
                .map(p -> new OptionDTO(p.getId(), p.getName()))
                .toList();
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        PracticeTemplate template = practiceTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template with ID " + id + " not found"));

        practiceTemplateRepository.delete(template); // Delete the template
    }

    /** Submit a test attempt: auto-scores MCQ (all-and-only correct). */

    // helpers
    private boolean blank(String s){ return s==null || s.isBlank(); }
    private int sanitize(Integer t){ return (t==null)?900:Math.max(60,t); }
    private Difficulty parseDiff(String s){ return blank(s)?Difficulty.MEDIUM:Difficulty.valueOf(s.trim().toUpperCase()); }
    private boolean hasRole(User u, String r){ return u.getRole()!=null && u.getRole().getName().equals(r); }
}
