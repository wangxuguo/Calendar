package com.oceansky.calendar.example.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * User: dengfa
 * Date: 16/8/1
 * Tel:  18500234565
 */
public class RegisterEntity {
    @SerializedName("access_token")
    String access_token;
    @SerializedName("token_type")
    String token_type;
    @SerializedName("expires_in")
    int    expires_in;
    @SerializedName("user_id")
    int    user_id;
    @SerializedName("extra")
    Extra  extra;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "RegisterData{" +
                "access_token='" + access_token + '\'' +
                ", token_type='" + token_type + '\'' +
                ", expires_in=" + expires_in +
                ", user_id=" + user_id +
                ", extra=" + extra +
                '}';
    }

    public static class Extra {
        @SerializedName("teacher_id")
        int teacher_id;

        @SerializedName("status")
        int status;

        @SerializedName("grade_id")
        int grade_id;

        @SerializedName("lesson_id")
        int lesson_id;

        public int getTeacher_id() {
            return teacher_id;
        }

        public void setTeacher_id(int teacher_id) {
            this.teacher_id = teacher_id;
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
            return "Extra{" +
                    "teacher_id=" + teacher_id +
                    ", status=" + status +
                    ", grade_id=" + grade_id +
                    ", lesson_id=" + lesson_id +
                    '}';
        }
    }
}
