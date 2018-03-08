package com.oceansky.calendar.example.event;

/**
 * User: dengfa
 * Date: 16/8/22
 * Tel:  18500234565
 */
public class CreatHomeworkEvent {
    private String homeworkTitle;

    public CreatHomeworkEvent(String homeworkTitle) {
        this.homeworkTitle = homeworkTitle;
    }

    public String getHomeworkTitle() {
        return homeworkTitle;
    }

    public void setHomeworkTitle(String homeworkTitle) {
        this.homeworkTitle = homeworkTitle;
    }
}
