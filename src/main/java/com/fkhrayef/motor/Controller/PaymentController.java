package com.fkhrayef.motor.Controller;

import com.fkhrayef.motor.Api.ApiResponse;
import com.fkhrayef.motor.DTOin.PaymentRequest;
import com.fkhrayef.motor.DTOout.MoyasarPaymentResponseDTO;
import com.fkhrayef.motor.DTOout.PaymentCreationResponseDTO;
import com.fkhrayef.motor.Model.Payment;
import com.fkhrayef.motor.Model.Subscription;
import com.fkhrayef.motor.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Simple payment processing
    @PostMapping("/card")
    public ResponseEntity<MoyasarPaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        MoyasarPaymentResponseDTO response = paymentService.processPayment(paymentRequest);
        return ResponseEntity.ok(response);
    }

    // Pay for subscription
    @PostMapping("/subscription/user/{userId}/plan/{planType}/billing/{billingCycle}")
    public ResponseEntity<?> payForSubscription(@PathVariable Integer userId,
                                                @PathVariable String planType,
                                                @PathVariable String billingCycle,
                                                @Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentCreationResponseDTO response = paymentService.createSubscriptionPayment(userId, planType, billingCycle, paymentRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // TODO: ADMIN or No one
    // Moyasar callback endpoint (just shows status)
    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestParam String id,
                                            @RequestParam String status,
                                            @RequestParam(required = false) String message) {
        // NO business logic - just return a message
        return ResponseEntity.ok(new ApiResponse(
                "Payment " + status + " processed. Moyasar ID: " + id +
                        (message != null ? ". Message: " + message : "")
        ));
    }

    // TODO: ADMIN or No one
    // Moyasar webhook endpoint (handles business logic)
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String payload) {
        // Process the webhook payload (includes secret token validation)
        paymentService.handleWebhook(payload);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Webhook processed successfully"));
    }

    // Simple payment status check endpoint (for frontend use)
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String paymentId) {
        String status = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Payment status: " + status));
    }

    // TODO: ADMIN
    // Get payment by ID
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable Integer paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.status(HttpStatus.OK).body(payment);
    }

    // Cancel subscription
    @PostMapping("/subscription/{userId}/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable Integer userId) {
        paymentService.cancelSubscription(userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Subscription cancelled successfully"));
    }

    // Get subscription status
    @GetMapping("/subscription/{userId}/status")
    public ResponseEntity<?> getSubscriptionStatus(@PathVariable Integer userId) {
        Subscription subscription = paymentService.getSubscriptionStatus(userId);
        return ResponseEntity.status(HttpStatus.OK).body(subscription);
    }

    // TODO: ADMIN
    // Get expiring subscriptions (admin endpoint)
    @GetMapping("/subscription/expiring")
    public ResponseEntity<?> getExpiringSubscriptions() {
        List<Subscription> expiringSubscriptions = paymentService.getExpiringSubscriptions();
        return ResponseEntity.status(HttpStatus.OK).body(expiringSubscriptions);
    }
}
