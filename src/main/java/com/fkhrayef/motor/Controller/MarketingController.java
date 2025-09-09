package com.fkhrayef.motor.Controller;

import com.fkhrayef.motor.Api.ApiResponse;
import com.fkhrayef.motor.DTOin.MarketingDTO;
import com.fkhrayef.motor.Service.MarketingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/marketing")
@RequiredArgsConstructor
public class MarketingController {

    private final MarketingService marketingService;

    // TODO: Admin endpoints
    @GetMapping("/get")
    public ResponseEntity<?> getAllMarketing (){
        return ResponseEntity.status(200).body(marketingService.getAllMarketing());
    }
    @PostMapping("/add")
    public ResponseEntity<?> addMarketing(@Valid @RequestBody MarketingDTO marketingDTO){
    marketingService.addMarketing(marketingDTO);
    return ResponseEntity.status(200).body(new ApiResponse("Marketing added successfully"));
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMarketing(@PathVariable Integer id , @Valid @RequestBody MarketingDTO marketingDTO){
        marketingService.updateMarketing(id, marketingDTO);
        return ResponseEntity.status(200).body(new ApiResponse("Marketing updated successfully"));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMarketing(@PathVariable Integer id){
        marketingService.deleteMarketing(id);
        return ResponseEntity.status(200).body(new ApiResponse("Marketing deleted successfully"));
    }
}
