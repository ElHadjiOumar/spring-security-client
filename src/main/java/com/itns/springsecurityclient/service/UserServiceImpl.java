package com.itns.springsecurityclient.service;

import org.springframework.stereotype.Service;

import com.itns.springsecurityclient.entity.PasswordResetToken;
import com.itns.springsecurityclient.entity.User;
import com.itns.springsecurityclient.entity.VerificationToken;
import com.itns.springsecurityclient.model.UserModel;
import com.itns.springsecurityclient.repository.PasswordResetTokenRepository;
import com.itns.springsecurityclient.repository.UserRepository;
import com.itns.springsecurityclient.repository.VerificationTokenRepository;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * This class is a service class that implements the UserService interface.
 */
@Service
public class UserServiceImpl implements UserService{

	 @Autowired
	    private UserRepository userRepository;

	    @Autowired
	    private VerificationTokenRepository verificationTokenRepository;

	    @Autowired
	    private PasswordResetTokenRepository passwordResetTokenRepository;

	    @Autowired
	    private PasswordEncoder passwordEncoder;

	    /**
		 * The function takes in a userModel object, creates a new user object, sets the user object's
		 * fields to the userModel's fields, sets the user object's role to "USER", and then saves the
		 * user object to the database
		 * 
		 * @param userModel This is the object that is passed from the controller.
		 * @return User
		 */
		@Override
	    public User registerUser(UserModel userModel) {
	        User user = new User();
	        user.setEmail(userModel.getEmail());
	        user.setFirstName(userModel.getFirstName());
	        user.setLastName(userModel.getLastName());
	        user.setRole("USER");
	        user.setPassword(passwordEncoder.encode(userModel.getPassword()));

	        userRepository.save(user);
	        return user;
	    } 
	    
	    /**
		 * It creates a new VerificationToken object, and then saves it to the database
		 * 
		 * @param token The token that will be sent to the user.
		 * @param user The user object that you want to save the token for.
		 */
		@Override
	    public void saveVerificationTokenForUser(String token, User user) {
	        VerificationToken verificationToken
	                = new VerificationToken(user, token);

	        verificationTokenRepository.save(verificationToken);
	    } 
	    
	    /**
		 * If the token is valid, the user is enabled and the token is deleted
		 * 
		 * @param token The token that was sent to the user's email address.
		 */
		@Override
	    public String validateVerificationToken(String token) {
	        VerificationToken verificationToken
	                = verificationTokenRepository.findByToken(token);

	        if (verificationToken == null) {
	            return "invalid";
	        }

	        User user = verificationToken.getUser();
	        Calendar cal = Calendar.getInstance();

	        if ((verificationToken.getExpirationTime().getTime()
	                - cal.getTime().getTime()) <= 0) {
	            verificationTokenRepository.delete(verificationToken);
	            return "expired";
	        }

	        user.setEnabled(true);
	        userRepository.save(user);
	        return "valid";
	    }
	    
	    /**
		 * It takes a token, finds the user associated with that token, generates a new token, and saves
		 * it to the database
		 * 
		 * @param oldToken The token that was sent to the user's email address.
		 * @return A new verification token is being returned.
		 */
		@Override
	    public VerificationToken generateNewVerificationToken(String oldToken) {
	        VerificationToken verificationToken
	                = verificationTokenRepository.findByToken(oldToken);
	        verificationToken.setToken(UUID.randomUUID().toString());
	        verificationTokenRepository.save(verificationToken);
	        return verificationToken;
	    }
	    
	    /**
		 * The function takes in a string, and returns a user object
		 * 
		 * @param email The email address of the user you want to find.
		 */
		@Override
	    public User findUserByEmail(String email) {
	        return userRepository.findByEmail(email);
	    }

	    /**
		 * It creates a new password reset token for the user and saves it to the database
		 * 
		 * @param user The user for whom the token is being created.
		 * @param token The token that will be sent to the user's email address.
		 */
		@Override
	    public void createPasswordResetTokenForUser(User user, String token) {
	        PasswordResetToken passwordResetToken
	                = new PasswordResetToken(user,token);
	        passwordResetTokenRepository.save(passwordResetToken);
	    }

	    /**
		 * If the token is not found in the database, return "invalid". If the token is found, but the
		 * expiration time is less than the current time, delete the token and return "expired". If the
		 * token is found and the expiration time is greater than the current time, return "valid"
		 * 
		 * @param token The token that was sent to the user's email address.
		 * @return A string.
		 */
		public String validatePasswordResetToken(String token) {
	        PasswordResetToken passwordResetToken
	                = passwordResetTokenRepository.findByToken(token);

	        if (passwordResetToken == null) {
	            return "invalid";
	        }

	        @SuppressWarnings("unused")
			User user = passwordResetToken.getUser();
	        Calendar cal = Calendar.getInstance();

	        if ((passwordResetToken.getExpirationTime().getTime()
	                - cal.getTime().getTime()) <= 0) {
	            passwordResetTokenRepository.delete(passwordResetToken);
	            return "expired";
	        }

	        return "valid";
	    }
	    
	    /**
		 * If the token is not null, return the user associated with the token.
		 * 
		 * @param token The token that was sent to the user's email address.
		 * @return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
		 */
		@Override
	    public Optional<User> getUserByPasswordResetToken(String token) {
	        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
	    }

	   /**
		* The function takes a user and a new password, sets the new password to the user, and saves the
		* user
		* 
		* @param user The user object that is being updated.
		* @param newPassword The new password to be set for the user.
		*/
	    @Override
	    public void changePassword(User user, String newPassword) {
	        user.setPassword(passwordEncoder.encode(newPassword));
	        userRepository.save(user);
	    }

	    /**
		 * It checks if the old password is valid by comparing the old password with the password in the
		 * database
		 * 
		 * @param user The user object that is currently logged in.
		 * @param oldPassword The password that the user entered in the form
		 * @return The passwordEncoder.matches() method returns a boolean value.
		 */
		@Override
	    public boolean checkIfValidOldPassword(User user, String oldPassword) {
	        return passwordEncoder.matches(oldPassword, user.getPassword());
	    }
}
