package com.anicictina.backend.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromAddress", "noreply@example.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");

        mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendsVerificationEmailWithLinkContainingToken() throws Exception {
        emailService.sendVerificationEmail("tina@example.com", "abc-123");

        verify(mailSender).send(mimeMessage);
        assertTrue(mimeMessage.getContent().toString().contains("http://localhost:4200/verify-email?token=abc-123"));
    }

    @Test
    void doesNotThrowWhenSendingFails() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("tina@example.com", "abc-123"));
    }
}
