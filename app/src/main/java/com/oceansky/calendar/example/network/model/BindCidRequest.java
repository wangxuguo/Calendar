package com.oceansky.calendar.example.network.model;

/**
 * User: dnegfa
 * Date: 16/10/14
 * Tel:  18500234565
 */
public class BindCidRequest {
    private String push_client_id;

    public BindCidRequest(String push_client_id) {
        this.push_client_id = push_client_id;
    }

    public String getPush_client_id() {
        return push_client_id;
    }

    public void setPush_client_id(String push_client_id) {
        this.push_client_id = push_client_id;
    }

    @Override
    public String toString() {
        return "BindCidRequest{" +
                "push_client_id='" + push_client_id + '\'' +
                '}';
    }
}
