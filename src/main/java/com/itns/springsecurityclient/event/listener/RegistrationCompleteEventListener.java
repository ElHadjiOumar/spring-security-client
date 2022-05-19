package com.itns.springsecurityclient.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.itns.springsecurityclient.entity.User;
import com.itns.springsecurityclient.event.RegistrationCompleteEvent;
import com.itns.springsecurityclient.service.UserService;

import java.util.UUID;

/**
 * This class listens for RegistrationCompleteEvent events and when it receives one, it sends an email
 * to the user.
 */
@Component
@Slf4j
public class RegistrationCompleteEventListener implements
        ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;

/**
 * The function is called when a user registers, and it creates a verification token for the user and
 * sends an email to the user with a link to verify the account
 * 
 * @param event The event that was fired.
 */
    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //Create the Verification Token for the User with Link
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(token,user);
        //Send Mail to user
        String url =
                event.getApplicationUrl()
                        + "/verifyRegistration?token="
                        + token;

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }
}
