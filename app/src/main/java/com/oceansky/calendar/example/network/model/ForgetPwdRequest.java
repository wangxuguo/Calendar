package com.oceansky.example.network.model;

/**
 * Created by tangqifa on 16/3/11.
 */
public class ForgetPwdRequest {
    private String phone_number;
    private String password;
    private String checkcode;

    public ForgetPwdRequest(String phone_number, String password, String checkcode) {
        this.phone_number = phone_number;
        this.password = password;
        this.checkcode = checkcode;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
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

    @Override
    public String toString() {
        return "ForgetPwdRequest{" +
                "phone_number='" + phone_number + '\'' +
                ", password='" + password + '\'' +
                ", checkcode='" + checkcode + '\'' +
                '}';
    }
}
