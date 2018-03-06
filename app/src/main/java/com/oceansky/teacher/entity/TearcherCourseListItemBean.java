package com.oceansky.teacher.entity;

import java.io.Serializable;

/**
 * 日历课程列表信息显示
 * User: 王旭国
 * Date: 16/6/24 11:51
 * Email:wangxuguo@jhyx.com.cn
 */
public class TearcherCourseListItemBean implements Serializable {
    private int    id;
    private String title;
    private String grade_id;
    private String lesson_id;
    private String grade_name;
    private String lesson_name;
    private String class_room;
    private String starttime;
    private String endtime;
    private String detail_url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGrade_id() {
        return grade_id;
    }

    public void setGrade_id(String grade_id) {
        this.grade_id = grade_id;
    }

    public String getLesson_id() {
        return lesson_id;
    }

    public void setLesson_id(String lesson_id) {
        this.lesson_id = lesson_id;
    }

    public String getGrade_name() {
        return grade_name;
    }

    public void setGrade_name(String grade_name) {
        this.grade_name = grade_name;
    }

    public String getLesson_name() {
        return lesson_name;
    }

    public void setLesson_name(String lesson_name) {
        this.lesson_name = lesson_name;
    }

    public String getClass_room() {
        return class_room;
    }

    public void setClass_room(String class_room) {
        this.class_room = class_room;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getDetail_url() {
        return detail_url;
    }

    public void setDetail_url(String detail_url) {
        this.detail_url = detail_url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TearcherCourseListItemBean that = (TearcherCourseListItemBean) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "TearcherCourseListItemBean{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", grade_id='" + grade_id + '\'' +
                ", lesson_id='" + lesson_id + '\'' +
                ", grade_name='" + grade_name + '\'' +
                ", lesson_name='" + lesson_name + '\'' +
                ", class_room='" + class_room + '\'' +
                ", starttime='" + starttime + '\'' +
                ", endtime='" + endtime + '\'' +
                ", detail_url='" + detail_url + '\'' +
                '}';
    }
}
