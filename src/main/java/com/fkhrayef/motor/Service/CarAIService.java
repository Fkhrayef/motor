package com.fkhrayef.motor.Service;

import com.fkhrayef.motor.Api.ApiException;
import com.fkhrayef.motor.DTOout.*;
import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Repository.CarRepository;
import com.fkhrayef.motor.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CarAIService {

    @Autowired
    private RAGService ragService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    private void validateSubscription(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        if (user.getSubscription() == null || 
            user.getSubscription().getStatus() == null || 
            !"active".equalsIgnoreCase(user.getSubscription().getStatus())) {
            throw new ApiException("AI features require an active subscription. Please upgrade to Pro or Enterprise plan.");
        }

        String planType = user.getSubscription().getPlanType();
        if ((!"pro".equalsIgnoreCase(planType) && !"enterprise".equalsIgnoreCase(planType))) {
            throw new ApiException("AI features require an active subscription. Please upgrade to Pro or Enterprise plan.");
        }
    }

    public ManualUploadResponse uploadManual(Integer userId, Integer carId, MultipartFile file) {
        // Validate user has active subscription
        validateSubscription(userId);

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        // Verify car belongs to user
        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("You don't have permission to upload manual for this car");
        }

        if (Boolean.FALSE.equals(car.getIsAccessible())) {
            throw new ApiException("This car is not accessible on your current plan.");
        }

        // Validate file type
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new ApiException("Only PDF files are allowed");
        }

        // Generate catalog path: YYYY-make-model-owner-manual.pdf
        String catalogPath = String.format("%d-%s-%s-owner-manual.pdf",
                car.getYear(),
                car.getMake().toLowerCase().replace(" ", "-"),
                car.getModel().toLowerCase().replace(" ", "-"));

        // Check if manual already exists in S3
        if (s3Service.catalogFileExists(catalogPath)) {
            throw new ApiException("Manual for this car already exists: " + catalogPath);
        }

        // Check if document already exists in RAG system
        String documentName = generateDocumentName(car);

        if (ragService.documentExists(documentName)) {
            throw new ApiException("Manual for this car already exists in the system: " + documentName);
        }

        // Upload to S3
        String s3Url;
        try {
            s3Url = s3Service.uploadCatalogFile(file, catalogPath);
        } catch (Exception e) {
            throw new ApiException("Failed to upload file: " + e.getMessage());
        }

        // Process document in RAG system
        boolean success = ragService.processDocument(s3Url, documentName);
        if (!success) {
            throw new ApiException("Failed to process document in RAG system");
        }

        ManualUploadResponse response = new ManualUploadResponse();
        response.setStatus("success");
        response.setMessage("Manual uploaded and processed successfully");
        response.setDocumentName(documentName);
        response.setS3Url(s3Url);
        response.setCarId(carId.toString());
        return response;
    }

    public QuestionResponse askQuestion(Integer userId, Integer carId, String question) {
        // Validate user has active subscription
        validateSubscription(userId);

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        // Verify car belongs to user
        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("You don't have permission to ask questions about this car");
        }

        if (Boolean.FALSE.equals(car.getIsAccessible())) {
            throw new ApiException("This car is not accessible on your current plan.");
        }

        if (question == null || question.trim().isEmpty()) {
            throw new ApiException("Question must not be empty");
        }

        // Generate document name from car details
        String documentName = generateDocumentName(car);

        // Check if document exists in RAG system
        if (!ragService.documentExists(documentName)) {
            throw new ApiException("Manual for this car is not available. Please upload the manual first.");
        }

        QuestionResponse response = ragService.askQuestion(question, documentName);
        return response;
    }

    public CarDocumentInfoResponse getCarDocumentInfo(Integer userId, Integer carId) {
        // Validate user has active subscription
        validateSubscription(userId);

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        // Verify car belongs to user
        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("You don't have permission to access information about this car");
        }

        // Generate document name from car details
        String documentName = generateDocumentName(car);

        boolean hasManual = ragService.documentExists(documentName);

        CarDocumentInfoResponse response = new CarDocumentInfoResponse();
        response.setCarId(carId.toString());
        response.setCarInfo(String.format("%d %s %s", car.getYear(), car.getMake(), car.getModel()));
        response.setDocumentName(documentName);
        response.setHasManual(hasManual ? "true" : "false");
        return response;
    }

    // Helper method to avoid code duplication
    private String generateDocumentName(Car car) {
        return String.format("%d %s %s owner-manual",
                car.getYear(),
                car.getMake(),
                car.getModel());
    }
}
