package com.fkhrayef.motor.Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final JavaMailSender mailSender;

    // TODO: Admin
    @GetMapping("/test")
    public ResponseEntity<String> sendTest() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("mshari.9420@gmail.com"); // بدّله بإيميلك الثاني
        msg.setSubject("Test Email from Motor");
        msg.setText("هذي رسالة تجريبية نصية من مشروع Motor 🚗");
        msg.setFrom(System.getenv("MAIL_USERNAME")); // يطابق اسم المستخدم
        mailSender.send(msg);
        return ResponseEntity.ok("Test email sent!");
    }
}