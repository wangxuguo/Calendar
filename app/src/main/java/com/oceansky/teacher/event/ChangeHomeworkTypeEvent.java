package com.oceansky.teacher.event;

/**
 * User: dengfa
 * Date: 16/9/8
 * Tel:  18500234565
 */
public class ChangeHomeworkTypeEvent {
    private int homeworkType;

    public ChangeHomeworkTypeEvent(int homeworkType) {
        this.homeworkType = homeworkType;
    }

    public int getHomeworkType() {
        return homeworkType;
    }

    public void setHomeworkType(int homeworkType) {
        this.homeworkType = homeworkType;
    }
}
