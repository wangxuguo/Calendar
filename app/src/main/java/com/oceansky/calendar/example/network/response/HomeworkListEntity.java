package com.oceansky.example.network.response;

import com.google.gson.annotations.SerializedName;
import com.oceansky.example.utils.MyHashSet;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * User: dengfa
 * Date: 16/6/16
 * Tel:  18500234565
 */
public class HomeworkListEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("list")
    private ArrayList<HomeworkData> homeworkList;

    public ArrayList<HomeworkData> getHomeworkList() {
        return homeworkList;
    }

    public void setHomeworkList(ArrayList<HomeworkData> homeworkList) {
        this.homeworkList = homeworkList;
    }

    @Override
    public String toString() {
        return "HomeworkListEntity{" +
                "homeworkList=" + homeworkList +
                '}';
    }

    public static class HomeworkData implements Serializable {
        private static final long serialVersionUID = 1L;

        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("grade_id")
        private int grade_id;

        @SerializedName("lesson_id")
        private int lesson_id;

        @SerializedName("textbook_ids")
        private MyHashSet<Integer> textbook_ids;

        @SerializedName("knowledge_chapter_ids")
        private MyHashSet<Integer> knowledge_chapter_ids;

        @SerializedName("knowledge_section_ids")
        private MyHashSet<Integer> knowledge_section_ids;

        @SerializedName("knowledge_detail_ids")
        private MyHashSet<Integer> knowledge_detail_ids;

        @SerializedName("question_ids")
        private MyHashSet<String> question_ids;
        @SerializedName("assigned")
        private int assigned;
        @SerializedName("course_id")
        private int course_id;

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

        public MyHashSet<Integer> getTextbook_ids() {
            return textbook_ids;
        }

        public void setTextbook_ids(MyHashSet<Integer> textbook_ids) {
            this.textbook_ids = textbook_ids;
        }

        public MyHashSet<Integer> getKnowledge_chapter_ids() {
            return knowledge_chapter_ids;
        }

        public void setKnowledge_chapter_ids(MyHashSet<Integer> knowledge_chapter_ids) {
            this.knowledge_chapter_ids = knowledge_chapter_ids;
        }

        public MyHashSet<Integer> getKnowledge_section_ids() {
            return knowledge_section_ids;
        }

        public void setKnowledge_section_ids(MyHashSet<Integer> knowledge_section_ids) {
            this.knowledge_section_ids = knowledge_section_ids;
        }

        public MyHashSet<Integer> getKnowledge_detail_ids() {
            return knowledge_detail_ids;
        }

        public void setKnowledge_detail_ids(MyHashSet<Integer> knowledge_detail_ids) {
            this.knowledge_detail_ids = knowledge_detail_ids;
        }

        public MyHashSet<String> getQuestion_ids() {
            return question_ids;
        }

        public void setQuestion_ids(MyHashSet<String> question_ids) {
            this.question_ids = question_ids;
        }

        public int getCourse_id() {
            return course_id;
        }

        public void setCourse_id(int course_id) {
            this.course_id = course_id;
        }

        public int getAssigned() {
            return assigned;
        }

        public void setAssigned(int assigned) {
            this.assigned = assigned;
        }

        @Override
        public String toString() {
            return "HomeworkData{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", grade_id=" + grade_id +
                    ", lesson_id=" + lesson_id +
                    ", textbook_ids=" + textbook_ids +
                    ", knowledge_chapter_ids=" + knowledge_chapter_ids +
                    ", knowledge_section_ids=" + knowledge_section_ids +
                    ", knowledge_detail_ids=" + knowledge_detail_ids +
                    ", question_ids=" + question_ids +
                    ", assigned=" + assigned +
                    ", course_id=" + course_id +
                    '}';
        }
    }
}