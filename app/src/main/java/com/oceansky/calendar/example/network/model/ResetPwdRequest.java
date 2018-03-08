package com.oceansky.calendar.example.network.model;

public class ResetPwdRequest {
    private String new_password;
    private String password;
    private String gt_clientID;

    public ResetPwdRequest(String password, String new_password, String gt_clientID) {
        this.new_password = new_password;
        this.password = password;
        this.gt_clientID = gt_clientID;
    }

    public String getNew_password() {
        return new_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGt_clientID() {
        return gt_clientID;
    }

    public void setGt_clientID(String gt_clientID) {
        this.gt_clientID = gt_clientID;
    }

    @Override
    public String toString() {
        return "PwdResetRequest{" +
                "new_password='" + new_password + '\'' +
                ", password='" + password + '\'' +
                ", gt_clientID='" + gt_clientID + '\'' +
                '}';
    }
}
