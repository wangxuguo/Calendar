package com.oceansky.example.event;

/**
 * User: dengfa
 * Date: 16/8/31
 * Tel:  18500234565
 */
public class DeleteHomeworkEvent {
    private String question_ids;

    public DeleteHomeworkEvent(String question_ids) {
        this.question_ids = question_ids;
    }

    public String getQuestion_ids() {
        return question_ids;
    }

    public void setQuestion_ids(String question_ids) {
        this.question_ids = question_ids;
    }
}
