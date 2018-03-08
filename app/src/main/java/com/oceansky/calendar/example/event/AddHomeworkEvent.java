package com.oceansky.example.event;

/**
 * User: dengfa
 * Date: 16/8/31
 * Tel:  18500234565
 */
public class AddHomeworkEvent {

    private String question_id;
    private String question_html;
    private int textbookId;
    private int chapterId;
    private int sectionId;
    private int detailId;

    public AddHomeworkEvent(String question_id, String question_html, int textbookId, int chapterId, int sectionId, int detailId) {
        this.question_id = question_id;
        this.question_html = question_html;
        this.textbookId = textbookId;
        this.chapterId = chapterId;
        this.sectionId = sectionId;
        this.detailId = detailId;
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

    public int getTextbookId() {
        return textbookId;
    }

    public void setTextbookId(int textbookId) {
        this.textbookId = textbookId;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }
}
