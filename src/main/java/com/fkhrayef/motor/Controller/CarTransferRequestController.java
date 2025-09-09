package com.fkhrayef.motor.Controller;

import com.fkhrayef.motor.DTOin.CarTransferResponseDTO;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Service.CarTransferRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transfer-requests")
@RequiredArgsConstructor
public class CarTransferRequestController {

    private final CarTransferRequestService transferService;


    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@AuthenticationPrincipal User user, @PathVariable Integer id){
        return ResponseEntity.status(200).body(transferService.accept(id, user.getId()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        return ResponseEntity.status(200).body(transferService.reject(id, user.getId()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@AuthenticationPrincipal User user, @PathVariable Integer id) {
        return ResponseEntity.status(200).body(transferService.cancel(id, user.getId()));
    }

    // TODO: ADMIN?
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(transferService.getOne(id));
    }

    @GetMapping("/incoming")
    public ResponseEntity<?> incoming(@AuthenticationPrincipal User user) {
        List<CarTransferResponseDTO> list = transferService.getIncoming(user.getId());
        return ResponseEntity.status(200).body(list);
    }

    @GetMapping("/outgoing")
    public ResponseEntity<?> outgoing(@AuthenticationPrincipal User user) {
        List<CarTransferResponseDTO> list = transferService.getOutgoing(user.getId());
        return ResponseEntity.status(200).body(list);
    }

    // TODO: ADMIN?
    @GetMapping("/by-car/{carId}")
    public ResponseEntity<?> byCar(@PathVariable Integer carId) {
        List<CarTransferResponseDTO> list = transferService.getByCar(carId);
        return ResponseEntity.status(200).body(list);
    }

    // TODO: ADMIN
    @GetMapping("/by-status/{status}")
    public ResponseEntity<?> byStatus(@PathVariable String status) {
        List<CarTransferResponseDTO> list = transferService.getByStatus(status);
        return ResponseEntity.status(200).body(list);
    }

    @PostMapping("/direct/{carId}/{toEmail}/{toPhone}")
    public ResponseEntity<?> directTransfer(@PathVariable Integer carId, @AuthenticationPrincipal User fromUser, @PathVariable String toEmail, @PathVariable String toPhone) {
        return ResponseEntity.status(200).body(transferService.directTransfer(carId, fromUser.getId(), toEmail, toPhone));
    }

}
