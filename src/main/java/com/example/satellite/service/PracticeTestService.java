package com.example.satellite.service;





import com.example.satellite.entity.*;
import com.example.satellite.entity.enums.AttemptStatus;
import com.example.satellite.payload.*;
import com.example.satellite.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
public class PracticeTestService {

    private final PracticeTemplateRepository templateRepo;
    private final PracticeTemplateLineRepository lineRepo;
    private final PracticeTemplateLineTopicRepository lineTopicRepo;

    private final PracticeTestRepository testRepo;
    private final PracticeTestQuestionRepository testQuestionRepo;

    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;

    private static final Pattern DATA_URL_PREFIX =
            Pattern.compile("^data:([\\w!#$&^_.+\\-]+/[\\w!#$&^_.+\\-]+)?;base64,", Pattern.CASE_INSENSITIVE);
    private final PracticeTestRepository practiceTestRepository;
    private final PracticeTestQuestionRepository practiceTestQuestionRepository;

    private final AnswerRepository answerRepository;
    private final PracticeTestResultRepository practiceTestResultRepository;


    public PracticeTestService(PracticeTemplateRepository templateRepo,
                               PracticeTemplateLineRepository lineRepo,
                               PracticeTemplateLineTopicRepository lineTopicRepo,
                               PracticeTestRepository testRepo,
                               PracticeTestQuestionRepository testQuestionRepo,
                               QuestionRepository questionRepo,
                               UserRepository userRepo, PracticeTestRepository practiceTestRepository, PracticeTestQuestionRepository practiceTestQuestionRepository, AnswerRepository a,
                               PracticeTestResultRepository p) {
        this.templateRepo = templateRepo;
        this.lineRepo = lineRepo;
        this.lineTopicRepo = lineTopicRepo;
        this.testRepo = testRepo;
        this.testQuestionRepo = testQuestionRepo;
        this.questionRepo = questionRepo;
        this.userRepo = userRepo;
        this.practiceTestRepository = practiceTestRepository;
        this.practiceTestQuestionRepository = practiceTestQuestionRepository;
        this.answerRepository = a;
        this.practiceTestResultRepository= p;
    }

    // service/PracticeTestReadService.java  (only method shown)
    @Transactional(readOnly = true)
    public TestContentDTO getTest(UUID testId, boolean embedImages) {
        PracticeTest test = testRepo.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("PracticeTest not found: " + testId));

        List<PracticeTestQuestion> links = practiceTestQuestionRepository.findAllByTestId(testId);
        links.sort(Comparator
                .comparingInt(PracticeTestQuestion::getPosition)
                .thenComparing(PracticeTestQuestion::getId));

        TestContentDTO dto = new TestContentDTO();
        dto.testId = test.getId();      // keep for back-compat
        dto.title  = test.getTitle();

        dto.items = links.stream().map(link -> {
            Question q = link.getQuestion();

            String qImg = toDataUrl(q.getImageContentType(), q.getImageBase64(), embedImages);

            List<AnswerOptionDTO> ans = (q.getAnswers() == null ? List.<AnswerOptionDTO>of()
                    : q.getAnswers().stream()
                    .sorted(Comparator
                            .comparing(Answer::getPosition)
                            .thenComparing(Answer::getId))
                    .map(a -> new AnswerOptionDTO(
                            a.getId(),                                  // NEW/ids
                            a.getText(),
                            toDataUrl(a.getImageContentType(), a.getImageBase64(), embedImages)
                    ))
                    .toList()
            );

            QuestionOptionDTO qdto = new QuestionOptionDTO(
                    q.getId(),                                             // NEW/ids
                    q.getText(),
                    q.getHint(),
                    qImg,
                    ans
            );

            TestContentDTO.Item item = new TestContentDTO.Item();
            item.position = link.getPosition();
            item.question = qdto;
            return item;
        }).toList();

        return dto;
    }

    // =======================
    // Start a test (per user)
    // =======================
    /**
     * Generate a unique PracticeTest for a specific user from a template.
     * - Validates the user has STUDENT role
     * - Pools questions by each template line (topics[] + difficulty)
     * - Prevents duplicates across lines
     * - Enforces allowPartial / shuffle from the template
     */
    @Transactional
    public StartTestResponse startFromTemplate(UUID templateId, UUID userId, String titleOverride) {
        if (templateId == null || userId == null) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "templateId and userId are required");
        }

        PracticeTemplate tpl = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Template not found: " + templateId));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found: " + userId));
        if (!hasRole(user, "STUDENT")) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "User must have STUDENT role");
        }

        // Build the randomized lineup
        List<Question> selected = pickQuestionsFromTemplate(tpl);

        // Persist PracticeTest header
        PracticeTest test = new PracticeTest();
        test.setTitle((titleOverride != null && !titleOverride.isBlank()) ? titleOverride.trim() : tpl.getTitle());
        test.setUser(user);
        test.setTimeLimitSec(tpl.getTimeLimitSec());
        test.setShuffle(tpl.isShuffle());
        test.setAllowPartial(tpl.isAllowPartial());
        Instant now = Instant.now();
        test.setStartedAt(now);
        test.setEndsAt(now.plusSeconds(tpl.getTimeLimitSec()));
        test.setStatus(AttemptStatus.ACTIVE);
        PracticeTest saved = testRepo.save(test);

        // Persist ordered links
        List<PracticeTestQuestion> links = new ArrayList<>(selected.size());
        for (int i = 0; i < selected.size(); i++) {
            PracticeTestQuestion link = new PracticeTestQuestion();
            link.setTest(saved);
            link.setQuestion(selected.get(i));
            link.setPosition(i);
            links.add(link);
        }
        testQuestionRepo.saveAll(links);

        StartTestResponse out = new StartTestResponse();
        out.testId = saved.getId();
        out.timeLimitSec = saved.getTimeLimitSec();
        out.startedAt = saved.getStartedAt();
        out.endsAt = saved.getEndsAt();
        out.totalQuestions = links.size();


        return out;
    }

    // ==================================
    // Get a test DTO (ordered questions)
    // ==================================
    @Transactional(readOnly = true)
    public PracticeTestDTO get(UUID testId) {
        PracticeTest test = testRepo.findById(testId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Test not found: " + testId));
        var links = testQuestionRepo.findByTestIdOrderByPositionAsc(testId);
        return PracticeTestDTO.of(test, links);
    }

    // ==========================================
    // List tests for a user (optionally by status)
    // ==========================================
    @Transactional(readOnly = true)
    public List<PracticeTestDTO> listForUser(UUID userId, AttemptStatus status) {
        if (userId == null) throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "userId required");
        List<PracticeTest> tests = (status == null)
                ? testRepo.findByUserIdOrderByCreatedAtDesc(userId)
                : testRepo.findByUserIdAndStatus(userId, status);

        List<PracticeTestDTO> out = new ArrayList<>(tests.size());
        for (PracticeTest t : tests) {
            var links = testQuestionRepo.findByTestIdOrderByPositionAsc(t.getId());
            out.add(PracticeTestDTO.of(t, links));
        }
        return out;
    }

    // =========================
    // Submit (grade & finalize)
    // =========================
    /**
     * Submits an active test owned by currentUserId.
     * Auto-scores MCQ: student must select all-and-only correct answers to get credit.
     * Open-ended (text) is stored and left for manual grading (correct = null).
     */


    // =================
    // Helper functions
    // =================
    private List<Question> pickQuestionsFromTemplate(PracticeTemplate tpl) {
        List<PracticeTemplateLine> lines = lineRepo.findByTemplateId(tpl.getId());
        List<Question> picked = new ArrayList<>();
        Set<UUID> used = new HashSet<>();

        for (PracticeTemplateLine line : lines) {
            List<Topic> topics = lineTopicRepo.findByLineId(line.getId()).stream()
                    .map(PracticeTemplateLineTopic::getTopic)
                    .toList();

            // Pool across topics for this difficulty
            List<Question> pool = new ArrayList<>();
            for (Topic t : topics) {
                pool.addAll(questionRepo.findByTopicIdAndDifficulty(t.getId(), line.getDifficulty()));
            }

            // Exclude already selected
            pool.removeIf(q -> used.contains(q.getId()));
            Collections.shuffle(pool, ThreadLocalRandom.current());

            int need = Math.max(0, line.getCount() == null ? 0 : line.getCount());

            if (!tpl.isAllowPartial() && pool.size() < need) {
                throw new ResponseStatusException(UNPROCESSABLE_ENTITY,
                        "Not enough " + line.getDifficulty() + " questions across selected topics. Need "
                                + need + ", have " + pool.size());
            }

            int take = Math.min(need, pool.size());
            for (int i = 0; i < take; i++) {
                Question q = pool.get(i);
                if (used.add(q.getId())) picked.add(q);
            }
        }

        if (tpl.isShuffle()) Collections.shuffle(picked, ThreadLocalRandom.current());
        return picked;
    }

    private boolean hasRole(User u, String r) {
        return u.getRole() != null && u.getRole().getName().contains(r);
    }

    private static String toDataUrl(String contentType, String maybeBase64, boolean embed) {
        if (!embed || maybeBase64 == null) return null;
        String v = maybeBase64.trim();
        if (v.isEmpty()) return null;

        // If it's already a data URL, just return it as-is.
        if (DATA_URL_PREFIX.matcher(v).find()) {
            return v;
        }

        // Treat as pure Base64. Strip whitespace/newlines to be safe.
        String pure = v.replaceAll("\\s+", "");

        // (Optional) validate Base64. If invalid, return null instead of throwing.
        try { Base64.getDecoder().decode(pure); } catch (IllegalArgumentException ex) { return null; }

        String ct = (contentType == null || contentType.isBlank())
                ? "application/octet-stream"
                : contentType;

        return "data:" + ct + ";base64," + pure;
    }



    @Transactional
    public SubmitPracticeTestResponse submit(UUID currentUserId,
                                             UUID pathPracticeTestId,
                                             SubmitPracticeTestRequest req) {
        UUID payloadId = (req != null) ? req.practiceTestId : null;
        UUID practiceTestId = (pathPracticeTestId != null) ? pathPracticeTestId : payloadId;
        if (practiceTestId == null) throw new IllegalArgumentException("practiceTestId is required");

        PracticeTest pt =  practiceTestRepository.findById(practiceTestId)
                .orElseThrow(() -> new NoSuchElementException("PracticeTest not found"));

        UUID templateId = pt.getTemplateId(); // ← from PracticeTest (since Question has no templateId)

        // Optional: verify ownership if PracticeTest has a userId field
        // if (pt.getUserId() != null && !pt.getUserId().equals(currentUserId)) {
        //     throw new SecurityException("Not your practice test");
        // }

        // Load all questionIds that belong to this practice test from DB
        List<PracticeTestQuestion> practiceTests = practiceTestQuestionRepository.findQuestionIdsByTestId(practiceTestId);
        Set<UUID> testQuestionSet = practiceTests.stream()
                .map(PracticeTestQuestion::getQuestion) // Question entity (may be a proxy)
                .filter(Objects::nonNull)
                .map(Question::getId)                   // <-- this is your getQuestion().getId()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<SubmitPracticeTestRequest.Item> items =
                (req != null && req.answers != null) ? req.answers : List.of();
        if (items.isEmpty()) throw new IllegalArgumentException("answers list is empty");

        // Validate submitted questions belong to this practice test
        boolean hasForeign = items.stream()
                .map(i -> i.questionId)
                .filter(Objects::nonNull)
                .anyMatch(qid -> !testQuestionSet.contains(qid));
        if (hasForeign) throw new IllegalArgumentException("Some questions do not belong to this practice test");

        // Load selected answers to evaluate correctness
        List<UUID> selectedAnswerIds = items.stream()
                .map(i -> i.answerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, Answer> ansById =
                selectedAnswerIds.isEmpty()
                        ? Map.of()
                        : answerRepository.findByIdIn(selectedAnswerIds).stream()
                        .collect(Collectors.toMap(a -> a.getId(), Function.identity()));

        // Optional integrity: ensure submitted questionIds actually exist (not just in mapping)
        List<UUID> questionIds = items.stream()
                .map(i -> i.questionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!questionIds.isEmpty()) {
            var found = questionRepo.findByIdIn(questionIds).stream()
                    .map(q -> q.getId()).collect(Collectors.toSet());
            boolean anyMissing = questionIds.stream().anyMatch(id -> !found.contains(id));
            if (anyMissing) throw new IllegalArgumentException("Some submitted questions do not exist");
        }

        // Evaluate
        int correct = 0;
        for (SubmitPracticeTestRequest.Item it : items) {
            if (it.questionId == null || it.answerId == null) continue;
            Answer a = ansById.get(it.answerId);
            boolean isCorrect = a != null
                    && Objects.equals(a.getQuestion().getId(), it.questionId)      // answer belongs to that question
                    && Boolean.TRUE.equals(a.isCorrect());                   // answer is marked correct
            if (isCorrect) correct++;
        }

        // Persist summary result row
        var result = new PracticeTestResult();
        result.setUserId(currentUserId);
        result.setTemplateId(templateId);
        result.setPracticeTestId(practiceTestId);
        result.setCorrectCount(correct);
        result.setTotalCount(practiceTests.size());
        result.setSubmittedAt(OffsetDateTime.now());
        result = practiceTestResultRepository.save(result);

        return new SubmitPracticeTestResponse(
                result.getId(),
                result.getPracticeTestId(),
                result.getTemplateId(),
                result.getCorrectCount(),
                result.getTotalCount(),
                result.getSubmittedAt()
        );
    }
}


