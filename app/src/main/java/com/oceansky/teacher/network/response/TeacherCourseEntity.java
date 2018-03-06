package com.oceansky.teacher.network.response;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * User: 王旭国
 * Date: 16/6/16 12:55
 * Email:wangxuguo@jhyx.com.cn
 */
public class TeacherCourseEntity implements Serializable {
    /**
     * id : 1
     * title :
     * lesson_name : 语文
     * grade_name : 高二
     * time_info : json string
     */
    private int                  id;
    private String               title;
    private String               grade_id;
    private String               lesson_id;
    private String               grade_name;
    private String               lesson_name;
    private String               class_room;
    private String               detail_url;
    /**
     * start_time : 08:00:00
     * end_time : 10:00:00
     */

    private List<ClassTimesBean> class_times;

    /**
     *
     */
    //        private JSONObject time_info;
    //
    private Map<String, List<Integer>> time_info;

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

    public List<ClassTimesBean> getClass_times() {
        return class_times;
    }

    public void setClass_times(List<ClassTimesBean> class_times) {
        this.class_times = class_times;
    }
    //
    //        public JSONObject getTime_info() {
    //            return time_info;
    //        }

    public Map<String, List<Integer>> getTime_info() {
        return time_info;
    }

    public void setTime_info(Map<String, List<Integer>> time_info) {
        this.time_info = time_info;
    }

    //        public void setTime_info(JSONObject time_info) {
    //            this.time_info = time_info;
    //        }

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
        TeacherCourseEntity that = (TeacherCourseEntity) o;
        if (id != that.id)
            return false;
        if (!title.equals(that.title))
            return false;
        if (!grade_id.equals(that.grade_id))
            return false;
        if (!lesson_id.equals(that.lesson_id))
            return false;
        if (!grade_name.equals(that.grade_name))
            return false;
        if (!lesson_name.equals(that.lesson_name))
            return false;
        if (!class_room.equals(that.class_room))
            return false;
        if (!class_times.equals(that.class_times))
            return false;
        return time_info.equals(that.time_info);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + title.hashCode();
        result = 31 * result + grade_id.hashCode();
        result = 31 * result + lesson_id.hashCode();
        result = 31 * result + grade_name.hashCode();
        result = 31 * result + lesson_name.hashCode();
        result = 31 * result + class_room.hashCode();
        result = 31 * result + class_times.hashCode();
        result = 31 * result + time_info.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TeacherCourseEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", grade_id='" + grade_id + '\'' +
                ", lesson_id='" + lesson_id + '\'' +
                ", grade_name='" + grade_name + '\'' +
                ", lesson_name='" + lesson_name + '\'' +
                ", class_room='" + class_room + '\'' +
                ", detail_url='" + detail_url + '\'' +
                ", class_times=" + class_times +
                ", time_info=" + time_info +
                '}';
    }

    public static class ClassTimesBean implements Serializable {
        private String start_time;
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
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ClassTimesBean that = (ClassTimesBean) o;

            if (start_time != null ? !start_time.equals(that.start_time) : that.start_time != null)
                return false;
            return end_time != null ? end_time.equals(that.end_time) : that.end_time == null;

        }

        @Override
        public int hashCode() {
            int result = start_time != null ? start_time.hashCode() : 0;
            result = 31 * result + (end_time != null ? end_time.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ClassTimesBean{" +
                    "start_time='" + start_time + '\'' +
                    ", end_time='" + end_time + '\'' +
                    '}';
        }
    }
}
