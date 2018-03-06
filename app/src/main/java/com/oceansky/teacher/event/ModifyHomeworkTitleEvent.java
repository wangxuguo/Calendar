package com.oceansky.teacher.event;

/**
 * User: dengfa
 * Date: 16/11/2
 * Tel:  18500234565
 */
public class ModifyHomeworkTitleEvent {
    private String homeworkTitle;

    public ModifyHomeworkTitleEvent(String homeworkTitle) {
        this.homeworkTitle = homeworkTitle;
    }

    public String getHomeworkTitle() {
        return homeworkTitle;
    }

    public void setHomeworkTitle(String homeworkTitle) {
        this.homeworkTitle = homeworkTitle;
    }
}
