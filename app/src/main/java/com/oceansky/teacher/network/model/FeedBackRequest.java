package com.oceansky.teacher.network.model;

public class FeedBackRequest {
    private String name;
    private String phone;
    private String message;

    public FeedBackRequest(String phone, String name, String message) {
        this.name = name;
        this.phone = phone;
        this.message = message;
    }
}
