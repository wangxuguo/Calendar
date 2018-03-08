package com.oceansky.calendar.example.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * User: dengfa
 * Date: 16/6/12
 * Tel:  18500234565
 */

public class ClassListEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}

