package com.oceansky.example.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class BaseDataEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("grades")
    private List<Data> grades;

    @SerializedName("educations")
    private List<Data> educations;

    @SerializedName("lessons")
    private List<Data> lessons;

    @SerializedName("textbooks")
    private List<Data> textbook;

    public List<Data> getGrades() {
        return grades;
    }

    public void setGrades(List<Data> grades) {
        this.grades = grades;
    }

    public List<Data> getEducations() {
        return educations;
    }

    public void setEducations(List<Data> educations) {
        this.educations = educations;
    }

    public List<Data> getLessons() {
        return lessons;
    }

    public void setLessons(List<Data> lessons) {
        this.lessons = lessons;
    }

    public List<Data> getTextbook() {
        return textbook;
    }

    public void setTextbook(List<Data> textbook) {
        this.textbook = textbook;
    }

    @Override
    public String toString() {
        return "BaseDataEntity{" +
                "grades=" + grades +
                ", educations=" + educations +
                ", lessons=" + lessons +
                ", textbook=" + textbook +
                '}';
    }

    public static class Data implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("id")
        private int    id;
        @SerializedName("name")
        private String name;

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

        @Override
        public String toString() {
            return "Grade{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}