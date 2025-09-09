package com.fkhrayef.motor.Controller;

import com.fkhrayef.motor.DTOout.*;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Service.CarAIService;
import com.fkhrayef.motor.Service.RAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/car-ai")
@RequiredArgsConstructor
public class CarAIController {

    private final CarAIService carAIService;

    private final RAGService ragService;

    @PostMapping("/upload-manual/{carId}")
    public ResponseEntity<?> uploadManual(
            @AuthenticationPrincipal User user,
            @PathVariable Integer carId,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ManualUploadResponse response = carAIService.uploadManual(user.getId(), carId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/ask/{carId}")
    public ResponseEntity<?> askQuestion(
            @AuthenticationPrincipal User user,
            @PathVariable Integer carId,
            @RequestParam String question) {

        QuestionResponse response = carAIService.askQuestion(user.getId(), carId, question);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/car/{carId}/info")
    public ResponseEntity<?> getCarDocumentInfo(@AuthenticationPrincipal User user, @PathVariable Integer carId) {
        CarDocumentInfoResponse info = carAIService.getCarDocumentInfo(user.getId(), carId);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    // TODO: Admin endpoints
    @GetMapping("/admin/documents")
    public ResponseEntity<?> getAllDocuments() {
        DocumentsInfoResponse documents = ragService.getDocumentsInfo();
        return ResponseEntity.status(HttpStatus.OK).body(documents);
    }

    @GetMapping("/admin/search")
    public ResponseEntity<?> searchDocuments(@RequestParam(required = false) String query) {
        DocumentsInfoResponse documents = ragService.getDocumentsInfo();
        List<String> documentNames = documents.getDocument_names() != null ? documents.getDocument_names() : List.of();
        
        if (query == null || query.trim().isEmpty()) {
            AllDocumentsResponse response = new AllDocumentsResponse();
            response.setDocuments(documentNames);
            response.setTotal_count(documentNames.size());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        
        List<String> filteredDocs = documentNames.stream()
                .filter(doc -> doc.toLowerCase().contains(query.toLowerCase()))
                .toList();
        
        DocumentSearchResponse response = new DocumentSearchResponse();
        response.setQuery(query);
        response.setDocuments(filteredDocs);
        response.setCount(filteredDocs.size());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/admin/check-document-exists")
    public ResponseEntity<?> checkDocumentExists(@RequestParam String documentName) {
        boolean exists = ragService.documentExists(documentName);
        DocumentExistsResponse response = new DocumentExistsResponse();
        response.setDocument_name(documentName);
        response.setExists(exists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
