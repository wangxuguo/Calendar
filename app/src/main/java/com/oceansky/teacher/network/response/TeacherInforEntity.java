package com.oceansky.teacher.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * User: dengfa
 * Date: 16/8/1
 * Tel:  18500234565
 */
public class TeacherInforEntity {

    @SerializedName("id")
    private int id;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("last_name")
    private String last_name;

    @SerializedName("sex")
    private int sex;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("first_year")
    private int first_year;

    @SerializedName("graduate")
    private String graduate;

    @SerializedName("education")
    private int education;

    @SerializedName("wechat_id")
    private String wechat;

    @SerializedName("email")
    private String email;

    @SerializedName("title_check")
    private int qualification;

    @SerializedName("ot_experience")
    private int experience;

    @SerializedName("status")
    private int status;

    @SerializedName("grade_id")
    int grade_id;

    @SerializedName("lesson_id")
    int lesson_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getFirst_year() {
        return first_year;
    }

    public void setFirst_year(int first_year) {
        this.first_year = first_year;
    }

    public String getGraduate() {
        return graduate;
    }

    public void setGraduate(String graduate) {
        this.graduate = graduate;
    }

    public int getEducation() {
        return education;
    }

    public void setEducation(int education) {
        this.education = education;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getQualification() {
        return qualification;
    }

    public void setQualification(int qualification) {
        this.qualification = qualification;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "TeacherInforEntity{" +
                "id=" + id +
                ", avatar='" + avatar + '\'' +
                ", last_name='" + last_name + '\'' +
                ", sex=" + sex +
                ", birthday='" + birthday + '\'' +
                ", first_year=" + first_year +
                ", graduate='" + graduate + '\'' +
                ", education=" + education +
                ", wechat='" + wechat + '\'' +
                ", email='" + email + '\'' +
                ", qualification=" + qualification +
                ", experience=" + experience +
                ", status=" + status +
                ", grade_id=" + grade_id +
                ", lesson_id=" + lesson_id +
                '}';
    }
}
