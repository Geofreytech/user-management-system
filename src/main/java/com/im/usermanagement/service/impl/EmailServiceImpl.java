package com.im.usermanagement.service.impl;

import com.im.usermanagement.model.User;
import com.im.usermanagement.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;

/**
 * Implementation of the EmailService.
 * NOTE: This requires 'spring-boot-starter-mail' dependency and mail configuration
 * (e.g., server, port, username, password) in application.properties.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    // private final JavaMailSender mailSender; // Uncomment if using Spring Mail

    /**
     * Sends a security notification to the user upon successful login.
     * In a real application, you would use 'mailSender' here.
     * For now, we will log the email instead of sending it.
     */
    @Override
    public void sendLoginNotification(User user) {
        log.info("Attempting to send security notification to: {}", user.getEmail());

        // --- REAL EMAIL IMPLEMENTATION WOULD GO HERE ---

        /*
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Security Alert: Successful Login");
        message.setText("Dear " + user.getFirstName() + ",\n\n"
                + "Your account was successfully accessed at " + new java.util.Date() + ".\n"
                + "If this was not you, please change your password immediately."
                + "\n\nUser Management System Security Team");
        mailSender.send(message);
        */

        // Placeholder Log:
        log.info("Login Notification successfully simulated for user: {}", user.getEmail());
        // ---------------------------------------------
    }
}