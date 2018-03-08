package com.oceansky.example.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * User: dengfa
 * Date: 16/8/31
 * Tel:  18500234565
 */
public class CreateHomeworkEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CreateHomeworkEntity{" +
                "id=" + id +
                '}';
    }
}
