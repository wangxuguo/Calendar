package com.oceansky.teacher.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * User: dengfa
 * Date: 16/7/21
 * Tel:  18500234565
 */
public class CourseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("status")
    private int status;

    @SerializedName("status_des")
    private String status_des;

    @SerializedName("grade_id")
    private int grade_id;

    @SerializedName("lesson_id")
    private int lesson_id;

    @SerializedName("lesson_name")
    private String lesson_name;

    @SerializedName("grade_name")
    private String grade_name;

    @SerializedName("start_date")
    private long start_date;

    @SerializedName("end_date")
    private long end_date;

    @SerializedName("class_room")
    private String class_room;

    @SerializedName("time_info")
    private ArrayList<Times> times;

    @SerializedName("detail_url")
    private String detail_url;

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatus_des() {
        return status_des;
    }

    public void setStatus_des(String status_des) {
        this.status_des = status_des;
    }

    public String getLesson_name() {
        return lesson_name;
    }

    public void setLesson_name(String lesson_name) {
        this.lesson_name = lesson_name;
    }

    public String getGrade_name() {
        return grade_name;
    }

    public void setGrade_name(String grade_name) {
        this.grade_name = grade_name;
    }

    public long getStart_date() {
        return start_date;
    }

    public void setStart_date(long start_date) {
        this.start_date = start_date;
    }

    public long getEnd_date() {
        return end_date;
    }

    public void setEnd_date(long end_date) {
        this.end_date = end_date;
    }

    public String getClass_room() {
        return class_room;
    }

    public void setClass_room(String class_room) {
        this.class_room = class_room;
    }

    public ArrayList<Times> getTimes() {
        return times;
    }

    public void setTimes(ArrayList<Times> times) {
        this.times = times;
    }

    public String getDetail_url() {
        return detail_url;
    }

    public void setDetail_url(String detail_url) {
        this.detail_url = detail_url;
    }

    @Override
    public String toString() {
        return "CourseEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", status_des='" + status_des + '\'' +
                ", lesson_name='" + lesson_name + '\'' +
                ", grade_name='" + grade_name + '\'' +
                ", start_date=" + start_date +
                ", end_date=" + end_date +
                ", class_room='" + class_room + '\'' +
                ", times=" + times +
                ", detail_url='" + detail_url + '\'' +
                '}';
    }

    public static class Times implements Serializable {
        private static final long serialVersionUID = 1L;

        @SerializedName("start_time")
        private String start_time;

        @SerializedName("end_time")
        private String end_time;

        public String getStart_time() {
            return start_time;
        }

        public void setStart_time(String start_time) {
            this.start_time = start_time;
        }

        public String getEnd_time() {
            return end_time;
        }

        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }

        @Override
        public String toString() {
            return "Times{" +
                    "start_time='" + start_time + '\'' +
                    ", end_time='" + end_time + '\'' +
                    '}';
        }
    }
}

