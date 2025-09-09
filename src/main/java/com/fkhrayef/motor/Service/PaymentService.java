package com.fkhrayef.motor.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fkhrayef.motor.Api.ApiException;
import com.fkhrayef.motor.DTOin.PaymentRequest;
import com.fkhrayef.motor.DTOout.MoyasarPaymentResponseDTO;
import com.fkhrayef.motor.DTOout.PaymentCreationResponseDTO;
import com.fkhrayef.motor.Model.Payment;
import com.fkhrayef.motor.Model.Subscription;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Repository.PaymentRepository;
import com.fkhrayef.motor.Repository.SubscriptionRepository;
import com.fkhrayef.motor.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    // Only the repositories we actually need
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final WhatsAppService whatsappService;

    @Value("${moyasar.api.key}")
    private String apiKey;

    @Value("${moyasar.webhook.secret}")
    private String webhookSecret;

    @Value("${APP_BASE_URL:http://localhost:8080}")
    private String baseUrl;

    // Simple hardcoded values
    private static final String MOYASAR_API_URL = "https://api.moyasar.com/v1";
    private static final String CURRENCY = "SAR";

    // Dynamic callback URL using environment variable
    private String getCallbackUrl() {
        return baseUrl + "/api/v1/payments/callback";
    }

    // Helper method to resolve transaction URL from Moyasar response
    private String resolveTransactionUrl(MoyasarPaymentResponseDTO moyasarResponse) {
        String transactionUrl = moyasarResponse.getTransaction_url();
        if (transactionUrl == null && moyasarResponse.getSource() != null) {
            transactionUrl = moyasarResponse.getSource().getTransaction_url();
        }
        return transactionUrl;
    }

    // Subscription pricing
    private static final Double PRO_MONTHLY_PRICE = 10.0;
    private static final Double PRO_YEARLY_PRICE = 100.0; // 10 months price
    private static final Double ENTERPRISE_MONTHLY_PRICE = 55.0;
    private static final Double ENTERPRISE_YEARLY_PRICE = 550.0; // 10 months price


    public MoyasarPaymentResponseDTO processPayment(PaymentRequest paymentRequest) {

        String url = MOYASAR_API_URL + "/payments";

        // create the request body
        String requestBody = String.format(
                "source[type]=card&source[name]=%s&source[number]=%s&source[cvc]=%s&" +
                        "source[month]=%s&source[year]=%s&amount=%d&currency=%s" +
                        "&callback_url=%s",
                paymentRequest.getName(),
                paymentRequest.getNumber(),
                paymentRequest.getCvc(),
                paymentRequest.getMonth(),
                paymentRequest.getYear(),
                (int) (paymentRequest.getAmount() * 100), // convert to the smallest currency unit
                paymentRequest.getCurrency(),
                getCallbackUrl()
        );

        // set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // send the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MoyasarPaymentResponseDTO> response = restTemplate.exchange(url,
                HttpMethod.POST, entity, MoyasarPaymentResponseDTO.class);

        // return the parsed DTO response
        return response.getBody();
    }

    public String getPaymentStatus(String paymentId) {
        // prepare headers with auth
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // create HTTP request entity
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // call Moyasar API
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                MOYASAR_API_URL + "/payments/" + paymentId, HttpMethod.GET, entity, String.class
        );

        // return the response
        return response.getBody();
    }

    // Simple payment creation methods
    public PaymentCreationResponseDTO createSubscriptionPayment(Integer userId, String planType, String billingCycle, PaymentRequest paymentRequest) {
        // Check if user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        // Check if user already has an active subscription
        Subscription existingSubscription = subscriptionRepository.findSubscriptionById(userId);
        if (existingSubscription != null) {
            throw new ApiException("User already has a subscription. Please cancel the current subscription before subscribing to a new plan.");
        }

        // Validate plan type and billing cycle
        if (!planType.equals("pro") && !planType.equals("enterprise")) {
            throw new ApiException("Invalid plan type. Must be 'pro' or 'enterprise'");
        }
        if (!billingCycle.equals("monthly") && !billingCycle.equals("yearly")) {
            throw new ApiException("Invalid billing cycle. Must be 'monthly' or 'yearly'");
        }

        // Calculate amount based on plan and billing cycle
        Double amount;
        if (planType.equals("pro")) {
            amount = billingCycle.equals("monthly") ? PRO_MONTHLY_PRICE : PRO_YEARLY_PRICE;
        } else {
            amount = billingCycle.equals("monthly") ? ENTERPRISE_MONTHLY_PRICE : ENTERPRISE_YEARLY_PRICE;
        }

        // Persist card details on the user for future renewals
        if (paymentRequest.getName() != null) user.setCardName(paymentRequest.getName());
        if (paymentRequest.getNumber() != null) user.setCardNumber(paymentRequest.getNumber());
        if (paymentRequest.getCvc() != null) user.setCardCvc(paymentRequest.getCvc());
        if (paymentRequest.getMonth() != null) user.setCardExpMonth(paymentRequest.getMonth());
        if (paymentRequest.getYear() != null) user.setCardExpYear(paymentRequest.getYear());
        userRepository.save(user);

        // Set payment details for Moyasar
        paymentRequest.setAmount(amount);
        paymentRequest.setDescription("Subscription: " + planType + " (" + billingCycle + ")");

        // Process payment through Moyasar
        MoyasarPaymentResponseDTO moyasarResponse = processPayment(paymentRequest);

        // Create payment record
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentType("subscription");
        payment.setStatus(moyasarResponse.getStatus() == null ? "pending" : moyasarResponse.getStatus().toLowerCase());
        payment.setCurrency(CURRENCY);
        payment.setDescription("Subscription: " + planType + " (" + billingCycle + ") - " + amount + " " + CURRENCY);
        payment.setUser(user);
        payment.setMoyasarPaymentId(moyasarResponse.getId());

        // Set null for unused reference fields to avoid constraint issues
        payment.setSubscription(null);

        Payment saved = paymentRepository.save(payment);

        // Don't create subscription yet - wait for payment completion via webhook
        // The subscription will be created when Moyasar sends "PAID" status

        String transactionUrl = resolveTransactionUrl(moyasarResponse);
        String message = "Payment initiated. Please complete payment using the provided link.";
        return new PaymentCreationResponseDTO(saved, transactionUrl, moyasarResponse.getId(), saved.getStatus(), message);
    }

    private boolean hasStoredCard(User user) {
        return user != null
                && user.getCardName() != null && !user.getCardName().isEmpty()
                && user.getCardNumber() != null && !user.getCardNumber().isEmpty()
                && user.getCardCvc() != null && !user.getCardCvc().isEmpty()
                && user.getCardExpMonth() != null && !user.getCardExpMonth().isEmpty()
                && user.getCardExpYear() != null && !user.getCardExpYear().isEmpty();
    }

    public Payment getPaymentById(Integer paymentId) {
        Payment payment = paymentRepository.findPaymentById(paymentId);
        if (payment == null) {
            throw new ApiException("Payment not found");
        }
        return payment;
    }

    /**
     * Handle payment completion - called when Moyasar webhook confirms payment
     * Creates subscription for subscription payments, updates balances for other payments
     */
    public void handlePaymentCompletion(String moyasarPaymentId, String moyasarStatus) {
        // Find payment by Moyasar ID
        Payment payment = paymentRepository.findByMoyasarPaymentId(moyasarPaymentId);
        if (payment == null) {
            throw new ApiException("Payment not found with Moyasar ID: " + moyasarPaymentId);
        }

        // Update payment status
        String newStatus = (moyasarStatus == null ? "pending" : moyasarStatus.toLowerCase());
        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        // Handle different payment types
        if ("subscription".equals(payment.getPaymentType()) && ("paid".equals(newStatus) || "captured".equals(newStatus))) {
            createSubscriptionFromPayment(payment);
            // Send payment completion notification to user
            sendPaymentCompletionNotification(payment, "subscription");
        }
    }

    /**
     * Create subscription from completed payment
     */
    private void createSubscriptionFromPayment(Payment payment) {
        try {
            // Parse plan details from payment description
            String description = payment.getDescription();
            // Format: "Subscription: pro (monthly) - 99.0 SAR"
            String[] parts = description.split(" - ")[0].split(": ")[1].split(" \\(");
            String planType = parts[0];
            String billingCycle = parts[1].replace(")", "");

            Integer userId = payment.getUser().getId();

            // Update existing subscription in place if present; otherwise create new
            Subscription subscription = subscriptionRepository.findSubscriptionById(userId);
            if (subscription == null) {
                subscription = new Subscription();
                subscription.setUser(payment.getUser()); // @MapsId will set ID from user
            }

            subscription.setPlanType(planType);
            subscription.setBillingCycle(billingCycle);
            subscription.setStatus("active");
            subscription.setStartDate(LocalDateTime.now());

            // Set end date based on billing cycle
            if (billingCycle.equals("monthly")) {
                subscription.setEndDate(LocalDateTime.now().plusMonths(1));
            } else {
                subscription.setEndDate(LocalDateTime.now().plusYears(1));
            }

//            // Set AI limits based on plan
//            if (planType.equals("pro")) {
//                // Update startup's daily AI limit
//                Startup startup = payment.getStartup();
//                startup.setDailyAiLimit(50); // Pro daily limit
//                startupRepository.save(startup);
//            } else {
//                // Update startup's daily AI limit
//                Startup startup = payment.getStartup();
//                startup.setDailyAiLimit(200); // Enterprise daily limit
//                startupRepository.save(startup);
//            }

            subscription.setPrice(payment.getAmount());

            subscriptionRepository.save(subscription);

            // Link payment to subscription
            payment.setSubscription(subscriptionRepository.findSubscriptionById(userId));
            paymentRepository.save(payment);

            // Send subscription activation notification to founder
            try {
                String userPhone = payment.getUser().getPhone();
                if (userPhone != null) {
                    String activationMessage = "🎉 تم تفعيل اشتراكك بنجاح\n\n" +
                            "📋 تفاصيل الاشتراك:\n" +
                            "• الخطة: " + planType + "\n" +
                            "• الدورة: " + billingCycle + "\n" +
                            "• المبلغ: " + payment.getAmount() + " " + payment.getCurrency() + "\n" +
                            "• تاريخ البداية: " + subscription.getStartDate().toLocalDate() + "\n" +
                            "• تاريخ الانتهاء: " + subscription.getEndDate().toLocalDate() + "\n\n" +
                            "مرحباً بك في منصتنا! 🚀";
                    whatsappService.sendWhatsAppMessage(activationMessage, userPhone);
                }
            } catch (Exception ex) {
                log.error("Failed to send subscription activation notification: {}", ex.getMessage());
            }

        } catch (Exception e) {
            throw new ApiException("Failed to create subscription from payment: " + e.getMessage());
        }
    }

    /**
     * Cancel a user's active subscription
     */
    public void cancelSubscription(Integer userId) {
        // Check if user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        // Find and delete subscription completely
        Subscription subscription = subscriptionRepository.findSubscriptionById(userId);
        if (subscription == null) {
            throw new ApiException("No subscription found for this user");
        }

        if (!"active".equals(subscription.getStatus())) {
            throw new ApiException("Subscription is not active. Current status: " + subscription.getStatus());
        }

        // Store subscription details for notification before deletion
        String planType = subscription.getPlanType();
        String billingCycle = subscription.getBillingCycle();

        // Properly handle the bidirectional relationship
        // Since @OneToOne with @PrimaryKeyJoinColumn, we need to clear the reference
        // This prevents JPA from trying to maintain the relationship
        user.setSubscription(null);

//        // Reset startup to free tier daily limits
//        startup.setDailyAiLimit(10); // Reset to free tier
//        startupRepository.save(startup);

        // Now we can safely delete the subscription
        subscriptionRepository.delete(subscription);

        // Verify the subscription was deleted
        if (subscriptionRepository.findSubscriptionById(userId) != null) {
            throw new ApiException("Failed to delete subscription");
        }

        // Send WhatsApp confirmation message to founder
        try {
            String userPhone = userRepository.findUserById(userId).getPhone();
            if (userPhone != null) {
                String message = "✅ تم إلغاء اشتراكك بنجاح\n\n" +
                        "📋 تفاصيل الاشتراك الملغي:\n" +
                        "• الخطة: " + planType + "\n" +
                        "• الدورة: " + billingCycle + "\n\n" +
                        "يمكنك إعادة الاشتراك في أي وقت";
                whatsappService.sendWhatsAppMessage(message, userPhone);
            }
        } catch (Exception ex) {
            // Log error but don't fail the main operation
            log.error("Failed to send WhatsApp cancellation confirmation: {}", ex.getMessage());
        }
    }

    /**
     * Get subscription status for a user
     */
    public Subscription getSubscriptionStatus(Integer userId) {
        // Check if user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        // Find subscription
        Subscription subscription = subscriptionRepository.findSubscriptionById(userId);
        if (subscription == null) {
            throw new ApiException("No subscription found for this user");
        }

        return subscription;
    }

    /**
     * Scheduled task to handle subscription renewals
     * Runs daily at midnight to check for expired subscriptions
     */
    // @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    @Scheduled(cron = "0 * * * * *") // Every minute (for testing)
    public void handleSubscriptionRenewals() {
        try {
            log.info("[Scheduler] Starting daily subscription renewal check...");
            // Find all active subscriptions that are expiring today or have expired
            List<Subscription> expiringSubscriptions = subscriptionRepository.findActiveSubscriptionsExpiringSoon(LocalDateTime.now().plusDays(1));

            for (Subscription subscription : expiringSubscriptions) {
                try {
                    log.info("[Scheduler] Processing renewal for userId={}, plan={}, cycle={}",
                            subscription.getUser() != null ? subscription.getUser().getId() : null,
                            subscription.getPlanType(),
                            subscription.getBillingCycle());
                    processSubscriptionRenewal(subscription);
                } catch (Exception e) {
                    // Continue with other subscriptions even if one fails
                    log.error("[Scheduler] Failed to process renewal: {}", e.getMessage());
                }
            }
            log.info("[Scheduler] Renewal check completed.");
        } catch (Exception e) {
            // Renewal job failed, will retry tomorrow
            log.error("[Scheduler] Renewal job failed: {}", e.getMessage());
        }
    }

    /**
     * Process renewal for a single subscription
     */
    private void processSubscriptionRenewal(Subscription subscription) {
        User user = subscription.getUser();

        // If no stored card data, cancel subscription and notify the user
        if (!hasStoredCard(user)) {
            try {
                cancelSubscription(user.getId());
            } catch (Exception ignored) {
            }

            try {
                String userPhone = user.getPhone();
                String message = "🚫 تم إلغاء اشتراكك\n\n" +
                        "السبب: معلومات الدفع غير متوفرة\n\n" +
                        "للمتابعة، يرجى إعادة الاشتراك مع تحديث بيانات البطاقة\n\n" +
                        "شكراً لك";
                log.info("[Scheduler][WhatsApp] To: {} | Message: {}", userPhone, message);
                if (userPhone != null) {
                    whatsappService.sendWhatsAppMessage(message, userPhone);
                }
                log.info("[Scheduler] Subscription cancelled due to missing card data. Notified: {}", userPhone);
            } catch (Exception ex) {
                log.error("[Scheduler] Failed to send WhatsApp cancel notification: {}", ex.getMessage());
            }
            return;
        }

        // Mark current subscription as expired
        subscription.setStatus("expired");
        subscriptionRepository.save(subscription);

        // Attempt automatic renewal using stored card fields
        try {
            PaymentRequest renewalRequest = new PaymentRequest();
            renewalRequest.setName(user.getCardName());
            renewalRequest.setNumber(user.getCardNumber());
            renewalRequest.setCvc(user.getCardCvc());
            renewalRequest.setMonth(user.getCardExpMonth());
            renewalRequest.setYear(user.getCardExpYear());
            renewalRequest.setAmount(subscription.getPrice());
            renewalRequest.setDescription("Auto-renewal: " + subscription.getPlanType() + " (" + subscription.getBillingCycle() + ")");
            renewalRequest.setCurrency("SAR");

            log.info("[Scheduler] Attempting auto-renewal charge for userId={}", user.getId());
            MoyasarPaymentResponseDTO moyasarResponse = processPayment(renewalRequest);

            Payment renewalPayment = new Payment();
            renewalPayment.setAmount(subscription.getPrice());
            renewalPayment.setPaymentType("subscription");
            renewalPayment.setStatus(moyasarResponse.getStatus() == null ? "pending" : moyasarResponse.getStatus().toLowerCase());
            renewalPayment.setCurrency("SAR");
            renewalPayment.setDescription("Auto-renewal: " + subscription.getPlanType() + " (" + subscription.getBillingCycle() + ")");
            renewalPayment.setUser(user);
            renewalPayment.setMoyasarPaymentId(moyasarResponse.getId());

            // Set null for unused reference fields
            renewalPayment.setSubscription(null);

            paymentRepository.save(renewalPayment);
            // Notify user via WhatsApp and also print message content for debugging
            try {
                String userPhone = user.getPhone();
                String paymentLink = resolveTransactionUrl(moyasarResponse);
                String successMessage = "🔄 تم بدء تجديد اشتراكك\n\n" +
                        "📋 تفاصيل التجديد:\n" +
                        "• الخطة: " + subscription.getPlanType() + "\n" +
                        "• الدورة: " + subscription.getBillingCycle() + "\n" +
                        "• المبلغ: " + renewalPayment.getAmount() + " " + renewalPayment.getCurrency() + "\n" +
                        "• الحالة: " + renewalPayment.getStatus() + "\n\n" +
                        "🔗 رابط الدفع:\n" + paymentLink + "\n\n" +
                        "يرجى إكمال الدفع لتفعيل اشتراكك";
                log.info("[Scheduler][WhatsApp] To: {} | Message: {}", userPhone, successMessage);
                if (userPhone != null) {
                    whatsappService.sendWhatsAppMessage(successMessage, userPhone);
                }
            } catch (Exception ex) {
                log.error("[Scheduler] Failed to send WhatsApp renewal notification: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.error("[Scheduler] Auto-renewal charge failed: {}", e.getMessage());
        }
    }

    /**
     * Send payment completion notifications to relevant parties
     */
    private void sendPaymentCompletionNotification(Payment payment, String paymentType) {
        try {
            User user = payment.getUser();

            // Notify user
            String userPhone = user.getPhone();
            if (userPhone != null) {
                String founderMessage = "✅ تم اكتمال الدفع بنجاح\n\n" +
                        "📋 تفاصيل العملية:\n" +
                        "• نوع الخدمة: " + getPaymentTypeInArabic(paymentType) + "\n" +
                        "• المبلغ: " + payment.getAmount() + " " + payment.getCurrency() + "\n" +
                        "• الحالة: مكتمل\n\n" +
                        "شكراً لك!";
                whatsappService.sendWhatsAppMessage(founderMessage, userPhone);
            }
        } catch (Exception ex) {
            log.error("Failed to send payment completion notifications: {}", ex.getMessage());
        }
    }

    /**
     * Get payment type in Arabic for notifications
     */
    private String getPaymentTypeInArabic(String paymentType) {
        switch (paymentType) {
            case "subscription":
                return "الاشتراك";
            case "freelancer_project":
                return "مشروع مستقل";
            case "advisor_session":
                return "جلسة استشارية";
            default:
                return "خدمة";
        }
    }

    /**
     * Get all active subscriptions that are expiring soon (within 1 day)
     */
    public List<Subscription> getExpiringSubscriptions() {
        return subscriptionRepository.findActiveSubscriptionsExpiringSoon(LocalDateTime.now().plusDays(1));
    }

    /**
     * Handle Moyasar webhook payload
     */
    public void handleWebhook(String payload) {
        try {
            // Parse the JSON payload
            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookData = mapper.readTree(payload);

            // Verify webhook secret token
            String secretToken = webhookData.path("secret_token").asText();
            if (!webhookSecret.equals(secretToken)) {
                throw new ApiException("Invalid webhook secret token");
            }

            // Extract payment information
            String type = webhookData.path("type").asText();

            // Handle payment_paid events
            if ("payment_paid".equals(type)) {
                JsonNode paymentData = webhookData.path("data");
                String paymentId = paymentData.path("id").asText();
                String status = paymentData.path("status").asText();

                log.info("[Webhook] Processing payment_paid: {} with status: {}", paymentId, status);
                handlePaymentCompletion(paymentId, status);
            } else {
                log.info("[Webhook] Ignoring non-payment_paid event: {}", type);
            }

        } catch (Exception e) {
            throw new ApiException("Failed to process webhook: " + e.getMessage());
        }
    }


}
