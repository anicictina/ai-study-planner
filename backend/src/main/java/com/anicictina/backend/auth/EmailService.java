package com.anicictina.backend.auth;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String to, String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Potvrdi svoj nalog — AI Study Planner");
            helper.setText(
                "Zdravo,\n\n"
                    + "Hvala što si se registrovala na AI Study Planner. Klikni link ispod da potvrdiš svoj nalog:\n\n"
                    + verificationLink
                    + "\n\n"
                    + "Link važi 24 sata. Ako nisi ti tražila ovaj nalog, slobodno ignoriši ovaj mejl.",
                false
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", to, e);
        }
    }
}
