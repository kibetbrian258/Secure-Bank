package com.application.secureBank.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.name:Secure Bank}")
    private String applicationName;

    /**
     * Send an email asynchronously
     *
     * @param to      Recipient email
     * @param subject Email subject
     * @param text    Email content (HTML)
     */
    @Async
    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // In a production environment, you might want to implement a retry mechanism
            // or queue failed emails for later retry
        }
    }

    /**
     * Send a registration confirmation email with credentials
     *
     * @param to          Recipient email
     * @param fullName    Customer's full name
     * @param customerId  Generated customer ID
     * @param pin         Generated PIN
     * @param accountNumber Generated account number
     */
    public void sendRegistrationEmail(String to, String fullName, String customerId, String pin, String accountNumber) {
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("customerId", customerId);
        context.setVariable("pin", pin);
        context.setVariable("accountNumber", accountNumber);
        context.setVariable("applicationName", applicationName);

        // If template engine fails to find the template, use a fallback plain HTML email
        String emailContent;
        try {
            emailContent = templateEngine.process("registration-email", context);
        } catch (Exception e) {
            log.warn("Email template not found, using fallback HTML email: {}", e.getMessage());
            emailContent = generateFallbackEmailContent(fullName, customerId, pin, accountNumber);
        }

        sendEmail(
                to,
                applicationName + " - Your Account Details",
                emailContent
        );
    }

    /**
     * Generate a fallback HTML email content if the template is not available
     */
    private String generateFallbackEmailContent(String fullName, String customerId, String pin, String accountNumber) {
        return "<html><body>" +
                "<h2>Welcome to " + applicationName + "!</h2>" +
                "<p>Dear " + fullName + ",</p>" +
                "<p>Your account has been created successfully. Please find your account details below:</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                "<p><strong>Customer ID:</strong> " + customerId + "</p>" +
                "<p><strong>PIN:</strong> " + pin + "</p>" +
                "<p><strong>Account Number:</strong> " + accountNumber + "</p>" +
                "</div>" +
                "<p>Please keep these details secure and do not share them with anyone.</p>" +
                "<p>You can use your Customer ID and PIN to log in to your account.</p>" +
                "<p>If you did not create this account, please contact our support team immediately.</p>" +
                "<p>Thank you for choosing " + applicationName + "!</p>" +
                "<p>Best regards,<br>The " + applicationName + " Team</p>" +
                "</body></html>";
    }
}