package com.fkhrayef.motor.Controller;

import com.fkhrayef.motor.Api.ApiResponse;
import com.fkhrayef.motor.DTOin.UserDTO;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Service.UserService;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // TODO: ADMIN
    @GetMapping("/get")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO ) {
        userService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("User registered successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal User user, @PathVariable Integer id, @Valid @RequestBody UserDTO userDTO) {
        userService.updateUser(user.getId(), id, userDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("User updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        userService.deleteUser(user.getId(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/upload-license/{id}")
    public ResponseEntity<?> uploadLicense(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("licenseExpiry") String licenseExpiry) {

        // TODO: Move logic into service
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("License file is required"));
        }

        if (licenseExpiry == null || licenseExpiry.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("License expiry date is required"));
        }

        try {
            // Parse the expiry date
            LocalDate expiryDate = LocalDate.parse(licenseExpiry);
            
            userService.uploadLicense(user.getId(), id, file, expiryDate);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("License uploaded successfully"));
            
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid date format. Please use yyyy-MM-dd format"));
        }
    }

    @GetMapping("/download-license/{id}")
    public ResponseEntity<?> downloadLicense(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        byte[] licenseData = userService.downloadLicense(user.getId(), id);
        
        // Generate filename for download
        String filename = String.format("user-%d-license.pdf", id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(licenseData);
    }

    @DeleteMapping("/delete-license/{id}")
    public ResponseEntity<?> deleteLicense(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        userService.deleteLicense(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse("License deleted successfully"));
    }

    @GetMapping("/{id}/subscription")
    public ResponseEntity<?> getSubscription(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        String type = userService.getUserSubscriptionType(user.getId(), id);

        Map<String, String> body = new HashMap<>();
        body.put("subscriptionType", type.isEmpty() ? "FREE" : type);

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}/card")
    public ResponseEntity<?> deleteUserCard(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        userService.deleteUserCard(user.getId(), id);
        return ResponseEntity.status(200).body(new ApiResponse("Card deleted successfully"));
    }
}
