package com.itns.springsecurityclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A POJO class that is used to store the data of the user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

	
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String matchingPassword;
    

}
