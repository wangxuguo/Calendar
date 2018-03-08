package com.oceansky.example.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * User: dengfa
 * Date: 16/6/12
 * Tel:  18500234565
 */

public class ClassEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("assistant")
    private Assistant assistant;

    @SerializedName("kids")
    private ArrayList<Kid> kids;

    public Assistant getAssistant() {
        return assistant;
    }

    public void setAssistant(Assistant assistant) {
        this.assistant = assistant;
    }

    public ArrayList<Kid> getKids() {
        return kids;
    }

    public void setKids(ArrayList<Kid> kids) {
        this.kids = kids;
    }

    @Override
    public String toString() {
        return "ClassData{" +
                "assistant=" + assistant +
                ", kids=" + kids +
                '}';
    }

    public static class Assistant implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("school_name")
        private String school_name;

        @SerializedName("avatar")
        private String avatar;

        @SerializedName("phone_number")
        private String phone_number;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSchool_name() {
            return school_name;
        }

        public void setSchool_name(String school_name) {
            this.school_name = school_name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        @Override
        public String toString() {
            return "Assistant{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", school_name='" + school_name + '\'' +
                    ", avatar='" + avatar + '\'' +
                    ", phone_number='" + phone_number + '\'' +
                    '}';
        }
    }

    public static class Kid implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("avatar")
        private String avatar;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        @Override
        public String toString() {
            return "Kid{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", avatar='" + avatar + '\'' +
                    '}';
        }
    }
}

