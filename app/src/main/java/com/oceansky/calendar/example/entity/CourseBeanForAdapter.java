package com.oceansky.calendar.example.entity;

import java.io.Serializable;

/**
 * User: dengfa
 * Date: 16/6/12
 * Tel:  18500234565
 */

public class CourseBeanForAdapter implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private String title;
    private int    status;
    private String status_des;
    private int    grade_id;
    private int    lesson_id;
    private String lesson_name;
    private String grade_name;
    private long   start_date;
    private long   end_date;
    private String class_room;
    private String start_time;
    private String end_time;
    private String detailUrl;

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

    public int getGrade_id() {
        return grade_id;
    }

    public void setGrade_id(int grade_id) {
        this.grade_id = grade_id;
    }

    public int getLesson_id() {
        return lesson_id;
    }

    public void setLesson_id(int lesson_id) {
        this.lesson_id = lesson_id;
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

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    @Override
    public String toString() {
        return "CourseBeanForAdapter{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", status_des='" + status_des + '\'' +
                ", grade_id=" + grade_id +
                ", lesson_id=" + lesson_id +
                ", lesson_name='" + lesson_name + '\'' +
                ", grade_name='" + grade_name + '\'' +
                ", start_date=" + start_date +
                ", end_date=" + end_date +
                ", class_room='" + class_room + '\'' +
                ", start_time='" + start_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", detailUrl='" + detailUrl + '\'' +
                '}';
    }
}

