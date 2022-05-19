package com.itns.springsecurityclient.controller;

import org.springframework.web.bind.annotation.RestController;

import com.itns.springsecurityclient.entity.User;
import com.itns.springsecurityclient.entity.VerificationToken;
import com.itns.springsecurityclient.event.RegistrationCompleteEvent;
import com.itns.springsecurityclient.model.PasswordModel;
import com.itns.springsecurityclient.model.UserModel;
import com.itns.springsecurityclient.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is a Spring Boot controller that handles requests to the /register endpoint.
 */
@RestController
@Slf4j
public class RegistrationController {

	 	// Injecting the UserService class into the RegistrationController class.
		 @Autowired 
	    private UserService userService;

	    // Publishing an event.
		@Autowired
	    private ApplicationEventPublisher publisher;

	    /**
		 * It takes a userModel object, creates a user object from it, and then publishes an event to the
		 * event bus
		 * 
		 * @param userModel This is the object that will be sent from the frontend.
		 * @param request The request object that contains the user's email address.
		 * @return A string "Success"
		 */
		@PostMapping("/register")
	    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
	        User user = userService.registerUser(userModel);
	        publisher.publishEvent(new RegistrationCompleteEvent(
	                user,
	                applicationUrl(request)
	        ));
	        return "Success";
	    } 
	    
	    /**
		 * This function is used to verify the user's email address
		 * 
		 * @param token The token that was sent to the user's email address.
		 * @return A string
		 */
		@GetMapping("/verifyRegistration")
	    public String verifyRegistration(@RequestParam("token") String token) {
	        String result = userService.validateVerificationToken(token);
	        if(result.equalsIgnoreCase("valid")) {
	            return "Utilisateur vérifié avec success";
	        }
	        return "Bad User";
	    }
	    
	    /**
		 * It takes the old token, generates a new token, sends the new token to the user's email, and
		 * returns a message to the user
		 * 
		 * @param oldToken The token that was sent to the user's email address.
		 * @param request The request object is used to get the URL of the application.
		 * @return A string
		 */
		@GetMapping("/resendVerifyToken")
	    public String resendVerificationToken(@RequestParam("token") String oldToken,
	                                          HttpServletRequest request) {
	        VerificationToken verificationToken
	                = userService.generateNewVerificationToken(oldToken);
	        User user = verificationToken.getUser();
	        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
	        return "Lien de verification envoyé";
	    }
	    
	    /**
		 * It takes the email address of the user, finds the user in the database, generates a random
		 * token, and sends an email to the user with a link to reset their password
		 * 
		 * @param passwordModel This is the object that contains the email address of the user.
		 * @param request The request object is used to get the URL of the application.
		 * @return The URL of the reset password page.
		 */
		@PostMapping("/resetPassword")
	    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
	        User user = userService.findUserByEmail(passwordModel.getEmail());
	        String url = "";
	        if(user!=null) {
	            String token = UUID.randomUUID().toString();
	            userService.createPasswordResetTokenForUser(user,token);
	            url = passwordResetTokenMail(user,applicationUrl(request), token);
	        }
	        return url;
	    }
	    
	    /**
		 * It takes a token and a password model as input, validates the token, and if the token is valid,
		 * it changes the password of the user associated with the token to the new password
		 * 
		 * @param token The token that was sent to the user's email address.
		 * @param passwordModel This is the object that will hold the new password.
		 * @return A string
		 */
		@PostMapping("/savePassword")
	    public String savePassword(@RequestParam("token") String token,
	                               @RequestBody PasswordModel passwordModel) {
	        String result = userService.validatePasswordResetToken(token);
	        if(!result.equalsIgnoreCase("valid")) {
	            return "Invalid Token";
	        }
	        Optional<User> user = userService.getUserByPasswordResetToken(token);
	        if(user.isPresent()) {
	            userService.changePassword(user.get(), passwordModel.getNewPassword());
	            return "Password Reset Successfully";
	        } else {
	            return "Invalid Token";
	        }
	    }
	    
	    /**
		 * It takes a JSON object with an email and a password, and then it changes the password of the
		 * user with that email to the new password
		 * 
		 * @param passwordModel This is the object that will be sent from the frontend.
		 * @return A String
		 */
		@PostMapping("/changePassword")
	    public String changePassword(@RequestBody PasswordModel passwordModel){
	        User user = userService.findUserByEmail(passwordModel.getEmail());
	        if(!userService.checkIfValidOldPassword(user,passwordModel.getOldPassword())) {
	            return "Invalid Old Password";
	        }
	        //Save New Password
	        userService.changePassword(user,passwordModel.getNewPassword());
	        return "Password Modifié avec Success";
	    }
	    
	    /**
		 * It sends a mail to the user with a link to reset the password.
		 * 
		 * @param user The user object that you want to send the email to.
		 * @param applicationUrl The URL of your application.
		 * @param token The token that was generated by the generatePasswordResetToken() method.
		 * @return The URL to reset the password.
		 */
		private String passwordResetTokenMail(User user, String applicationUrl, String token) {
	        String url =
	                applicationUrl
	                        + "/savePassword?token="
	                        + token;

	        //sendVerificationEmail()
	        log.info("Cliquer sur le link pour reinitialiser votre Mot de Passe: {}",
	                url);
	        return url;
	    }
	    
	   /**
		* It sends a verification email to the user.
		* 
		* @param user The user object that was created.
		* @param applicationUrl The URL of your application.
		* @param verificationToken The token that was generated when the user registered.
		*/
	    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
	        String url =
	                applicationUrl
	                        + "/verifyRegistration?token="
	                        + verificationToken.getToken();

	        //sendVerificationEmail()
	        log.info("Cliquer sur le link pour verifier votre account: {}",
	                url);
	    }
	    
	    /**
		 * It returns the URL of the application, including the port number
		 * 
		 * @param request The HttpServletRequest object.
		 * @return The application URL.
		 */
		private String applicationUrl(HttpServletRequest request) {
	        return "http://" +
	                request.getServerName() +
	                ":" +
	                request.getServerPort() +
	                request.getContextPath();
	    }
}
