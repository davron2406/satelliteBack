package com.example.satellite.controller;//package com.example.satellite.controller;
//
//
//import com.example.satellite.payload.AnswerDTO;
//import com.example.satellite.service.AnswerService;
//import jakarta.validation.constraints.NotNull;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.UUID;
//
//@Validated
//@RestController
//@RequestMapping("/api")
//public class AnswerController {
//
//    private final AnswerService service;
//    public AnswerController(AnswerService service) { this.service = service; }
//
//    // Create answer under a question
//    @PostMapping(path = "/questions/{questionId}/answers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<AnswerDTO> create(
//            @PathVariable UUID questionId,
//            @RequestParam(required = false) String text,
//            @RequestParam(required = false) Boolean correct,
//            @RequestParam(required = false) Integer position,
//            @RequestPart(required = false) MultipartFile image
//    ) throws Exception {
//        return ResponseEntity.ok(service.create(questionId, text, correct, position, image));
//    }
//
//    // Update an answer (text/correct/position/image)
//    @PutMapping(path = "/answers/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<AnswerDTO> update(
//            @PathVariable UUID id,
//            @RequestParam(required = false) String text,
//            @RequestParam(required = false) Boolean correct,
//            @RequestParam(required = false) Integer position,
//            @RequestPart(required = false) MultipartFile image,
//            @RequestParam(required = false, defaultValue = "false") Boolean removeImage
//    ) throws Exception {
//        return ResponseEntity.ok(service.update(id, text, correct, position, image, removeImage));
//    }
//
//    @DeleteMapping("/answers/{id}")
//    public ResponseEntity<Void> delete(@PathVariable UUID id) {
//        service.delete(id); return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/answers/{id}")
//    public ResponseEntity<AnswerDTO> get(@PathVariable UUID id) {
//        return ResponseEntity.ok(service.get(id));
//    }
//
//    @GetMapping("/questions/{questionId}/answers")
//    public ResponseEntity<List<AnswerDTO>> list(@PathVariable @NotNull UUID questionId) {
//        return ResponseEntity.ok(service.listByQuestion(questionId));
//    }
//}
