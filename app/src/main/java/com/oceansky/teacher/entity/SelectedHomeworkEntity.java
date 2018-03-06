package com.oceansky.teacher.entity;

import java.io.Serializable;

/**
 * User: dengfa
 * Date: 16/9/1
 * Tel:  18500234565
 */
public class SelectedHomeworkEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    textbook_id;
    private int    knowledge_chapter_id;
    private int    knowledge_section_id;
    private int    knowledge_detail_id;
    private String question_id;
    private String question_html;

    public SelectedHomeworkEntity(int textbook_id, int knowledge_chapter_id, int knowledge_section_id,
                                  int knowledge_detail_id, String question_id, String question_html) {
        this.textbook_id = textbook_id;
        this.knowledge_chapter_id = knowledge_chapter_id;
        this.knowledge_section_id = knowledge_section_id;
        this.knowledge_detail_id = knowledge_detail_id;
        this.question_id = question_id;
        this.question_html = question_html;
    }

    public int getTextbook_id() {
        return textbook_id;
    }

    public void setTextbook_id(int textbook_id) {
        this.textbook_id = textbook_id;
    }

    public int getKnowledge_chapter_id() {
        return knowledge_chapter_id;
    }

    public void setKnowledge_chapter_id(int knowledge_chapter_id) {
        this.knowledge_chapter_id = knowledge_chapter_id;
    }

    public int getKnowledge_section_id() {
        return knowledge_section_id;
    }

    public void setKnowledge_section_id(int knowledge_section_id) {
        this.knowledge_section_id = knowledge_section_id;
    }

    public int getKnowledge_detail_id() {
        return knowledge_detail_id;
    }

    public void setKnowledge_detail_id(int knowledge_detail_id) {
        this.knowledge_detail_id = knowledge_detail_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getQuestion_html() {
        return question_html;
    }

    public void setQuestion_html(String question_html) {
        this.question_html = question_html;
    }

    @Override
    public String toString() {
        return "SelectedHomeworkEntity{" +
                "textbook_id=" + textbook_id +
                ", knowledge_chapter_id=" + knowledge_chapter_id +
                ", knowledge_section_id=" + knowledge_section_id +
                ", knowledge_detail_id=" + knowledge_detail_id +
                ", question_id='" + question_id + '\'' +
                ", question_html='" + question_html + '\'' +
                '}';
    }
}
