package com.oceansky.teacher.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * User: dengfa
 * Date: 16/8/29
 * Tel:  18500234565
 */
public class KnowledgePointEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("knowledge_chapter")
    private ArrayList<ChapterData> knowledge_chapter;

    public ArrayList<ChapterData> getKnowledge_chapter() {
        return knowledge_chapter;
    }

    public void setKnowledge_chapter(ArrayList<ChapterData> knowledge_chapter) {
        this.knowledge_chapter = knowledge_chapter;
    }

    @Override
    public String toString() {
        return "KnowledgePointEntity{" +
                "knowledge_chapter=" + knowledge_chapter +
                '}';
    }

    /**
     * 章节信息
     */
    public static class ChapterData implements Serializable {
        private static final long serialVersionUID = 1L;

        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("knowledge_section")
        private ArrayList<SectionData> knowledge_section;

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

        public ArrayList<SectionData> getKnowledge_section() {
            return knowledge_section;
        }

        public void setKnowledge_section(ArrayList<SectionData> knowledge_section) {
            this.knowledge_section = knowledge_section;
        }

        @Override
        public String toString() {
            return "ChapterData{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", knowledge_section=" + knowledge_section +
                    '}';
        }
    }

    /**
     * 章节下的大知识点
     */
    public static class SectionData implements Serializable {
        private static final long serialVersionUID = 1L;

        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("knowledge_detail")
        private ArrayList<SectionDetail> knowledge_detail;

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

        public ArrayList<SectionDetail> getKnowledge_detail() {
            return knowledge_detail;
        }

        public void setKnowledge_detail(ArrayList<SectionDetail> knowledge_detail) {
            this.knowledge_detail = knowledge_detail;
        }

        @Override
        public String toString() {
            return "SectionData{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", knowledge_detail=" + knowledge_detail +
                    '}';
        }
    }

    /**
     * 大知识点下的详细知识点
     */
    public static class SectionDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        @SerializedName("id")
        private int id;

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
            return "SectionDetail{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}