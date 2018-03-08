package com.oceansky.calendar.example.network.response;

import com.google.gson.annotations.SerializedName;

public class ModifyInforEntity {

    @SerializedName("avatar")
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "BaseData{" +
                "avatar='" + avatar + '\'' +
                '}';
    }
}
