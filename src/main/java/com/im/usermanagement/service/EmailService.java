package com.im.usermanagement.service;

import com.im.usermanagement.model.User;

/**
 * Service contract for handling email operations, such as login notifications.
 */
public interface EmailService {
    /**
     * Sends a security notification to the user upon successful login.
     * @param user The User object who just logged in.
     */
    void sendLoginNotification(User user);
}