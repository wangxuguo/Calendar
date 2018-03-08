package com.oceansky.calendar.example.event;

/**
 * User: dengfa
 * Date: 16/10/31
 * Tel:  18500234565
 */
public class HomeworkOutOfLimitEvent {

    private String question_id;

    public HomeworkOutOfLimitEvent(String question_id) {
        this.question_id = question_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }
}
