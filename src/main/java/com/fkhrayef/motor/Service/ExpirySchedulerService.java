package com.fkhrayef.motor.Service;
import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Repository.CarRepository;
import com.fkhrayef.motor.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpirySchedulerService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final EmailService emailService;

    // كل يوم 9 صباحًا
    @Scheduled(cron = "0 0 9 * * *")
    public void sendLicenseExpiryAlerts() {
        LocalDate target = LocalDate.now().plusMonths(1); // بعد شهر
        List<User> users = userRepository.findByLicenseExpiry(target);

        for (User u : users) {
            try {
                if (u.getEmail() == null || u.getEmail().isBlank()) continue;
                if (u.getLicenseExpiry() == null) continue;

                String subject = "تنبيه: انتهاء رخصتك بعد شهر";
                String html = buildLicenseHtml(u);

                emailService.sendEmailHtml(u.getEmail(), subject, html);

            } catch (Exception e) {
                log.warn("Failed to send license expiry email: userId={}, email={}",
                        u.getId(), u.getEmail(), e);
            }
        }
    }

    // كل يوم 9:10 صباحًا
    @Scheduled(cron = "0 10 9 * * *")
    public void sendCarExpiryAlerts() {
        LocalDate target = LocalDate.now().plusMonths(1); // بعد شهر

        // الاستمارة
        List<Car> regCars = carRepository.findByRegistrationExpiry(target);
        for (Car c : regCars) {
            try {
                if (c.getRegistrationExpiry() == null) continue;
                if (c.getUser() == null) continue;
                User u = c.getUser();
                if (u.getEmail() == null || u.getEmail().isBlank()) continue;

                String subject = "تنبيه: انتهاء استمارة سيارتك بعد شهر";
                String html = buildRegistrationHtml(c, u);

                emailService.sendEmailHtml(u.getEmail(), subject, html);

            } catch (Exception e) {
                log.warn("Failed to send registration expiry email: carId={}, userId={}, email={}",
                        c.getId(), c.getUser()!=null?c.getUser().getId():null,
                        c.getUser()!=null?c.getUser().getEmail():null, e);
            }
        }

        // التأمين
        List<Car> insCars = carRepository.findByInsuranceEndDate(target);
        for (Car c : insCars) {
            try {
                if (c.getInsuranceEndDate() == null) continue;
                if (c.getUser() == null) continue;
                User u = c.getUser();
                if (u.getEmail() == null || u.getEmail().isBlank()) continue;

                String subject = "تنبيه: انتهاء تأمين سيارتك بعد شهر";
                String html = buildInsuranceHtml(c, u);

                emailService.sendEmailHtml(u.getEmail(), subject, html);

            } catch (Exception e) {
                log.warn("Failed to send insurance expiry email: carId={}, userId={}, email={}",
                        c.getId(), c.getUser()!=null?c.getUser().getId():null,
                        c.getUser()!=null?c.getUser().getEmail():null, e);
            }
        }
    }

    // ================== HTML Templates ==================

    private String shell(String emoji, String title, String accentColor, String content) {
        return """
    <!DOCTYPE html>
    <html lang="ar" dir="rtl">
    <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <title>%s</title>
    </head>
    <body style="margin:0;background:#f6f7f9;font-family:Tahoma,Arial,sans-serif;line-height:1.9;color:#0f172a">
      <div style="max-width:600px;margin:24px auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:14px;overflow:hidden">
        
        <!-- Header -->
        <div style="background:%s;color:#fff;padding:16px 20px;display:flex;align-items:center;gap:10px">
          <div style="font-size:24px">%s</div>
          <div style="font-size:16px;font-weight:700"> %s </div>
          <div style="margin-inline-start:auto;font-size:14px;opacity:.9">Motor 🚗</div>
        </div>

        <!-- Body -->
        <div style="padding:22px">
          %s
          <div style="margin-top:18px;padding:12px 14px;border:1px dashed #e5e7eb;border-radius:10px;font-size:12px;color:#64748b">
            هذه رسالة تذكير آلية من تطبيق Motor.
          </div>
        </div>
      </div>
    </body>
    </html>
    """.formatted(title, accentColor, emoji, title, content);
    }

    private String buildLicenseHtml(User u) {
        String date = u.getLicenseExpiry() != null ? DATE_FMT.format(u.getLicenseExpiry()) : "-";
        String content = """
        <p style="margin:0 0 10px;font-size:16px">مرحبًا %s 👋</p>

        <div style="background:#fff7ed;border:1px solid #fed7aa;border-radius:12px;padding:14px 16px;margin:10px 0">
          <div style="font-weight:700;margin-bottom:6px">🪪 رخصة القيادة</div>
          <div style="font-size:15px">📅 تاريخ الانتهاء:</div>
          <div style="font-size:20px;font-weight:800;margin-top:4px;letter-spacing:.3px">%s</div>
        </div>

        <ul style="margin:14px 0 0;padding:0 18px;color:#334155;font-size:14px">
          <li>هذا التذكير يُرسل قبل شهر من موعد الانتهاء.</li>
        </ul>
    """.formatted(u.getName(), date);

        return shell("🪪", "تنبيه انتهاء الرخصة", "#f59e0b", content); // Amber
    }

    private String buildRegistrationHtml(Car c, User u) {
        String date = c.getRegistrationExpiry() != null ? DATE_FMT.format(c.getRegistrationExpiry()) : "-";
        String content = """
        <p style="margin:0 0 10px;font-size:16px">مرحبًا %s 👋</p>

        <div style="background:#eff6ff;border:1px solid #bfdbfe;border-radius:12px;padding:14px 16px;margin:10px 0">
          <div style="font-weight:700;margin-bottom:6px">📄 استمارة المركبة</div>
          <div style="font-size:15px">📅 تاريخ الانتهاء:</div>
          <div style="font-size:20px;font-weight:800;margin-top:4px;letter-spacing:.3px">%s</div>
        </div>

        <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:12px 14px;margin-top:12px">
          <div style="font-weight:700;margin-bottom:8px">🚗 تفاصيل السيارة</div>
          <ul style="margin:0;padding:0 18px;color:#334155;font-size:14px">
            <li>🏷️ الماركة: %s</li>
            <li>🚘 الموديل: %s</li>
            <li>📆 السنة: %s</li>
          </ul>
        </div>

        <ul style="margin:14px 0 0;padding:0 18px;color:#334155;font-size:14px">
          <li>هذا التذكير يُرسل قبل شهر من موعد الانتهاء.</li>
        </ul>
    """.formatted(u.getName(), date, c.getMake(), c.getModel(), c.getYear());

        return shell("📄", "تنبيه انتهاء الاستمارة", "#3b82f6", content); // Blue
    }

    private String buildInsuranceHtml(Car c, User u) {
        String date = c.getInsuranceEndDate() != null ? DATE_FMT.format(c.getInsuranceEndDate()) : "-";
        String content = """
        <p style="margin:0 0 10px;font-size:16px">مرحبًا %s 👋</p>

        <div style="background:#ecfdf5;border:1px solid #bbf7d0;border-radius:12px;padding:14px 16px;margin:10px 0">
          <div style="font-weight:700;margin-bottom:6px">🛡️ تأمين المركبة</div>
          <div style="font-size:15px">📅 تاريخ الانتهاء:</div>
          <div style="font-size:20px;font-weight:800;margin-top:4px;letter-spacing:.3px">%s</div>
        </div>

        <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:12px 14px;margin-top:12px">
          <div style="font-weight:700;margin-bottom:8px">🚗 تفاصيل السيارة</div>
          <ul style="margin:0;padding:0 18px;color:#334155;font-size:14px">
            <li>🏷️ الماركة: %s</li>
            <li>🚘 الموديل: %s</li>
            <li>📆 السنة: %s</li>
          </ul>
        </div>

        <ul style="margin:14px 0 0;padding:0 18px;color:#334155;font-size:14px">
          <li>هذا التذكير يُرسل قبل شهر من موعد الانتهاء.</li>
        </ul>
    """.formatted(u.getName(), date, c.getMake(), c.getModel(), c.getYear());

        return shell("🛡️", "تنبيه انتهاء التأمين", "#10b981", content); // Green
    }
}