package com.oceansky.teacher.network.model;

/**
 * Created by tangqifa on 16/3/11.
 */
public class RegisterRequest {
    private String username;
    private String password;
    private String checkcode;
    private String grant_type;


    public RegisterRequest(String grant_type, String username, String password, String checkcode) {
        this.username = username;
        this.password = password;
        this.checkcode = checkcode;
        this.grant_type = grant_type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCheckcode() {
        return checkcode;
    }

    public void setCheckcode(String checkcode) {
        this.checkcode = checkcode;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", checkcode='" + checkcode + '\'' +
                ", grant_type='" + grant_type + '\'' +
                '}';
    }
}
