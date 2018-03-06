package com.oceansky.teacher.event;

/**
 * User: dengfa
 * Date: 16/8/31
 * Tel:  18500234565
 */
public class SelectHomeworkEvent {

    private String question_id;
    private String question_html;

    public SelectHomeworkEvent(String question_id, String question_html) {
        this.question_id = question_id;
        this.question_html = question_html;
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
}
