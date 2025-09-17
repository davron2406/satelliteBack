package com.example.satellite.service;//package com.example.satellite.service;
//
//
//
//import com.example.satellite.entity.Answer;
//import com.example.satellite.entity.Question;
//import com.example.satellite.payload.AnswerDTO;
//import com.example.satellite.repository.AnswerRepository;
//import com.example.satellite.repository.QuestionRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.UUID;
//
//@Service
//public class AnswerService {
//
//    private final AnswerRepository answerRepo;
//    private final QuestionRepository questionRepo;
//
//    public AnswerService(AnswerRepository answerRepo, QuestionRepository questionRepo) {
//        this.answerRepo = answerRepo;
//        this.questionRepo = questionRepo;
//    }
//
//    // -------- CREATE --------
//    @Transactional
//    public AnswerDTO create(UUID questionId, String text, Boolean correct, Integer position, MultipartFile image)
//            throws IOException {
//        Question q = questionRepo.findById(questionId)
//                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
//
//        long count = answerRepo.countByQuestionId(questionId);
//        if (count >= 4) throw new IllegalStateException("A question can have at most 4 answers");
//
//        short pos = computeInsertPosition(questionId, count, position);
//
//        // shift answers at/after pos up by 1 (to keep unique (question,position))
//        shiftUpFromPosition(questionId, pos);
//
//        Answer a = new Answer();
//        a.setQuestion(q);
//        a.setPosition(pos);
//        a.setText(safe(text));
//        a.setCorrect(Boolean.TRUE.equals(correct));
//
//        if (image != null && !image.isEmpty()) {
//            setImage(a, image);
//        }
//
//        return AnswerDTO.fromEntity(answerRepo.save(a));
//    }
//
//    // -------- UPDATE --------
//    @Transactional
//    public AnswerDTO update(UUID answerId, String text, Boolean correct, Integer newPosition,
//                            MultipartFile image, Boolean removeImage) throws IOException {
//        Answer a = answerRepo.findById(answerId)
//                .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));
//
//        if (text != null) a.setText(text);
//        if (correct != null) a.setCorrect(correct);
//
//        if (Boolean.TRUE.equals(removeImage)) {
//            a.setImageData(null); a.setImageContentType(null); a.setImageFilename(null);
//        } else if (image != null && !image.isEmpty()) {
//            setImage(a, image);
//        }
//
//        if (newPosition != null) {
//            short target = clampPos(newPosition);
//            if (target != a.getPosition()) {
//                reorderWithinQuestion(a, target);
//            }
//        }
//
//        return AnswerDTO.fromEntity(answerRepo.save(a));
//    }
//
//    // -------- DELETE --------
//    @Transactional
//    public void delete(UUID answerId) {
//        Answer a = answerRepo.findById(answerId)
//                .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));
//
//        UUID qid = Objects.requireNonNull(a.getQuestion()).getId();
//        short removedPos = a.getPosition();
//        answerRepo.delete(a);
//
//        // shift answers after removedPos down by 1
//        List<Answer> rest = answerRepo.findByQuestionIdOrderByPositionAsc(qid);
//        for (Answer x : rest) {
//            if (x.getPosition() > removedPos) x.setPosition((short)(x.getPosition() - 1));
//        }
//        answerRepo.saveAll(rest);
//    }
//
//    // -------- READ --------
//    @Transactional(readOnly = true)
//    public AnswerDTO get(UUID id) {
//        return answerRepo.findById(id).map(AnswerDTO::fromEntity)
//                .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + id));
//    }
//
//    @Transactional(readOnly = true)
//    public List<AnswerDTO> listByQuestion(UUID questionId) {
//        List<Answer> list = answerRepo.findByQuestionIdOrderByPositionAsc(questionId);
//        List<AnswerDTO> out = new ArrayList<>(list.size());
//        for (Answer a : list) out.add(AnswerDTO.fromEntity(a));
//        return out;
//    }
//
//    // -------- helpers --------
//    private void setImage(Answer a, MultipartFile image) throws IOException {
//        if (image.getSize() > 5 * 1024 * 1024) {
//            throw new IllegalArgumentException("Image too large (max 5MB)");
//        }
//        String ct = image.getContentType();
//        if (ct == null || !ct.startsWith("image/")) throw new IllegalArgumentException("Only image files allowed");
//
//        a.setImageData(image.getBytes());
//        a.setImageContentType(ct);
//        a.setImageFilename(image.getOriginalFilename());
//    }
//
//    private String safe(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
//
//    private short computeInsertPosition(UUID questionId, long currentCount, Integer requested) {
//        if (requested == null) return (short) currentCount; // append
//        short p = clampPos(requested);
//        // if position already taken, we will shift up in shiftUpFromPosition
//        return p;
//    }
//
//    private short clampPos(int p) {
//        if (p < 0) return 0;
//        if (p > 3) return 3;
//        return (short) p;
//    }
//
//    private void shiftUpFromPosition(UUID questionId, short fromPos) {
//        List<Answer> list = answerRepo.findByQuestionIdOrderByPositionAsc(questionId);
//        for (int i = list.size() - 1; i >= 0; i--) {
//            Answer a = list.get(i);
//            if (a.getPosition() >= fromPos && a.getPosition() < 3) {
//                a.setPosition((short)(a.getPosition() + 1));
//            }
//        }
//        answerRepo.saveAll(list);
//    }
//
//    private void reorderWithinQuestion(Answer a, short target) {
//        UUID qid = Objects.requireNonNull(a.getQuestion()).getId();
//        short current = a.getPosition();
//        if (target == current) return;
//
//        List<Answer> list = answerRepo.findByQuestionIdOrderByPositionAsc(qid);
//        // remove current from list
//        list.removeIf(x -> x.getId().equals(a.getId()));
//
//        // shift others to make space at target
//        for (Answer x : list) {
//            short p = x.getPosition();
//            if (target < current) { // moving up
//                if (p >= target && p < current) x.setPosition((short)(p + 1));
//            } else { // moving down
//                if (p <= target && p > current) x.setPosition((short)(p - 1));
//            }
//        }
//        a.setPosition(target);
//        list.add(a);
//        answerRepo.saveAll(list);
//    }
//}
//
