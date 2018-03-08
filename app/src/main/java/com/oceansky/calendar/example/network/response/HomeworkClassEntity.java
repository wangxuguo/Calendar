package com.oceansky.calendar.example.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * User: dengfa
 * Date: 16/9/5
 * Tel:  18500234565
 */
public class HomeworkClassEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("current_time")
    private long current_time;

    @SerializedName("items")
    private ArrayList<ClassData> classList;

    public long getCurrent_time() {
        return current_time;
    }

    public void setCurrent_time(long current_time) {
        this.current_time = current_time;
    }

    public ArrayList<ClassData> getClassList() {
        return classList;
    }

    public void setClassList(ArrayList<ClassData> classList) {
        this.classList = classList;
    }

    @Override
    public String toString() {
        return "HomeworkClassEntity{" +
                "current_time=" + current_time +
                ", classList=" + classList +
                '}';
    }

    public static class ClassData{
        @SerializedName("id")
        private int classId;

        @SerializedName("title")
        private String classTitle;

        public int getClassId() {
            return classId;
        }

        public void setClassId(int classId) {
            this.classId = classId;
        }

        public String getClassTitle() {
            return classTitle;
        }

        public void setClassTitle(String classTitle) {
            this.classTitle = classTitle;
        }

        @Override
        public String toString() {
            return "ClassData{" +
                    "classId=" + classId +
                    ", classTitle='" + classTitle + '\'' +
                    '}';
        }
    }
}