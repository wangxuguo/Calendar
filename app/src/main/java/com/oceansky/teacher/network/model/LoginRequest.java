package com.oceansky.teacher.network.model;

/**
 * Created by tangqifa on 16/3/11.
 */
public class LoginRequest {
    private String username;
    private String password;
    private String grant_type;

    public LoginRequest(String grant_type, String username, String password) {
        this.username = username;
        this.password = password;
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

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", grant_type='" + grant_type + '\'' +
                '}';
    }
}
