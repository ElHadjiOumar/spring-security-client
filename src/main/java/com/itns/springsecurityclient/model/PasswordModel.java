package com.itns.springsecurityclient.model;

import lombok.Data;

/**
 * A POJO class that is used to store the password.
 */
@Data
public class PasswordModel {

    private String email;
    private String oldPassword;
    private String newPassword;
}