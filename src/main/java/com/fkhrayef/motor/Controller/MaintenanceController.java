package com.fkhrayef.motor.Controller;

import com.fkhrayef.motor.Api.ApiResponse;
import com.fkhrayef.motor.DTOin.MaintenanceDTO;
import com.fkhrayef.motor.Model.Maintenance;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // TODO: ADMIN
    @GetMapping("/get")
    public ResponseEntity<?> getAllMaintenances() {
        return ResponseEntity.status(HttpStatus.OK).body(maintenanceService.getAllMaintenances());
    }

    @PostMapping("/add/{carId}")
    public ResponseEntity<?> addMaintenance(@AuthenticationPrincipal User user, @PathVariable Integer carId, @Valid @RequestBody MaintenanceDTO maintenanceDTO) {
        maintenanceService.addMaintenance(user.getId(), carId, maintenanceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Maintenance added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMaintenance(@AuthenticationPrincipal User user, @PathVariable Integer id, @Valid @RequestBody MaintenanceDTO maintenanceDTO) {
        maintenanceService.updateMaintenance(user.getId(), id, maintenanceDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Maintenance updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMaintenance(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        maintenanceService.deleteMaintenance(user.getId(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/get/{carId}")
    public ResponseEntity<?> getMaintenancesByCarId(@AuthenticationPrincipal User user, @PathVariable Integer carId) {
        return ResponseEntity.status(HttpStatus.OK).body(maintenanceService.getMaintenancesByCarId(user.getId(), carId));
    }

    // Invoice file management endpoints
    @PostMapping("/upload-invoice/{id}")
    public ResponseEntity<?> uploadInvoice(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("invoiceAmount") Double invoiceAmount) {

        // TODO: Move logic to service

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invoice file is required"));
        }

        if (invoiceAmount == null || invoiceAmount <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invoice amount is required and must be greater than 0"));
        }

        try {
            maintenanceService.uploadInvoice(user.getId(), id, file, invoiceAmount);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Invoice uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage()));
        }
    }

    @GetMapping("/download-invoice/{id}")
    public ResponseEntity<?> downloadInvoice(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        byte[] invoiceData = maintenanceService.downloadInvoice(user.getId(), id);
        
        // Get maintenance details to include service type in filename
        Maintenance maintenance = maintenanceService.getMaintenanceById(id);
        String serviceType = maintenance.getServiceType();
        
        // Clean service type for filename (remove special characters)
        String cleanServiceType = serviceType.replaceAll("[^a-zA-Z0-9\\s-]", "").replaceAll("\\s+", "-");
        String filename = String.format("%s-invoice-%d.pdf", cleanServiceType, id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(invoiceData);
    }

    @DeleteMapping("/delete-invoice/{id}")
    public ResponseEntity<?> deleteInvoice(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        maintenanceService.deleteInvoice(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("Invoice deleted successfully"));
    }

}
