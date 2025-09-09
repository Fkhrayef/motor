package com.fkhrayef.motor.Controller;

import com.fkhrayef.motor.Api.ApiResponse;
import com.fkhrayef.motor.DTOin.CarDTO;
import com.fkhrayef.motor.DTOin.CarMileageUpdateDTO;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    // TODO: ADMIN
    @GetMapping("/get")
    public ResponseEntity<?> getAllCars() {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getAllCars());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCar(@AuthenticationPrincipal User user, @Valid @RequestBody CarDTO carDTO) {
        carService.addCar(user.getId(), carDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Car added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCar(@AuthenticationPrincipal User user, @PathVariable Integer id, @Valid @RequestBody CarDTO carDTO) {
        carService.updateCar(user.getId(), id, carDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Car updated successfully"));
    }

    @PutMapping("/update/{carId}/mileage")
    public ResponseEntity<?> updateMileage(@AuthenticationPrincipal User user, @PathVariable Integer carId, @Valid @RequestBody CarMileageUpdateDTO dto) {
        carService.updateMileage(user.getId(), carId, dto.getMileage());
        return ResponseEntity.ok(new ApiResponse("Mileage updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCar(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        carService.deleteCar(user.getId(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/get/user")
    public ResponseEntity<?> getCarsByUserId(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getCarsByUserId(user.getId()));
    }

    // Registration file management endpoints
    @PostMapping("/upload-registration/{id}")
    public ResponseEntity<?> uploadRegistration(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("registrationExpiry") String registrationExpiry) {

        // TODO: Move logic to service

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Registration file is required"));
        }

        if (registrationExpiry == null || registrationExpiry.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Registration expiry date is required"));
        }

        try {
            LocalDate expiryDate = LocalDate.parse(registrationExpiry);
            carService.uploadRegistration(user.getId(), id, file, expiryDate);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Registration uploaded successfully"));
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid date format. Please use yyyy-MM-dd format"));
        }
    }

    @GetMapping("/download-registration/{id}")
    public ResponseEntity<?> downloadRegistration(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        byte[] registrationData = carService.downloadRegistration(user.getId(), id);
        String filename = String.format("car-%d-registration.pdf", id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(registrationData);
    }

    @DeleteMapping("/delete-registration/{id}")
    public ResponseEntity<?> deleteRegistration(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        carService.deleteRegistration(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("Registration deleted successfully"));
    }

    // Insurance file management endpoints
    @PostMapping("/upload-insurance/{id}")
    public ResponseEntity<?> uploadInsurance(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("insuranceEndDate") String insuranceEndDate) {

        // TODO: Move logic to service

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Insurance file is required"));
        }

        if (insuranceEndDate == null || insuranceEndDate.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Insurance end date is required"));
        }

        try {
            LocalDate endDate = LocalDate.parse(insuranceEndDate);
            carService.uploadInsurance(user.getId(), id, file, endDate);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Insurance uploaded successfully"));
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid date format. Please use yyyy-MM-dd format"));
        }
    }

    @GetMapping("/download-insurance/{id}")
    public ResponseEntity<?> downloadInsurance(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        byte[] insuranceData = carService.downloadInsurance(user.getId(), id);
        String filename = String.format("car-%d-insurance.pdf", id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(insuranceData);
    }

    @DeleteMapping("/delete-insurance/{id}")
    public ResponseEntity<?> deleteInsurance(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        carService.deleteInsurance(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("Insurance deleted successfully"));
    }

    // Yearly maintenance cost (last 12 months)
    @GetMapping("/maintenance-cost/{make}/{model}/yearly")
    public ResponseEntity<?>  getYearlyMaintenanceCost(
            @AuthenticationPrincipal User user,
            @PathVariable String make,
            @PathVariable String model,
            @RequestParam(required = false) Integer minMileage,
            @RequestParam(required = false) Integer maxMileage
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getMaintenanceCostOneYear(user.getId(), make, model, minMileage, maxMileage));
    }

    // Visit frequency (last 12 months), phrased as “once every ~X years”
    @GetMapping("/visit-frequency/{make}/{model}")
    public ResponseEntity<?>  getVisitFrequency(
            @AuthenticationPrincipal User user,
            @PathVariable String make,
            @PathVariable String model,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getVisitFrequency(user.getId(), make, model, minAge, maxAge));
    }

    //Get the typical mileage per year for a given car make/model.
    @GetMapping("/typical-mileage/{make}/{model}")
    public ResponseEntity<?>  getTypicalMileagePerYear(
            @AuthenticationPrincipal User user,
            @PathVariable String make,
            @PathVariable String model,
            @RequestParam(required = false) String city
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getTypicalMileagePerYear(user.getId(), make, model, city));
    }

    // TODO: Admin
    @GetMapping("/numbers/{userId}")
    public ResponseEntity<?> getCarsNo(@PathVariable Integer userId) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getCarsNumbers(userId));
    }

    // TODO: Admin
    @PutMapping("/{userId}/enforce-access")
    public ResponseEntity<?> enforceAccess(@PathVariable Integer userId) {
        carService.enforceCarAccess(userId);
        return ResponseEntity.ok().build();
    }

}
