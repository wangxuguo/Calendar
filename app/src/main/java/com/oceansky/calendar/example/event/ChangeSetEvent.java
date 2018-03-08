package com.oceansky.example.event;

/**
 * User: dengfa
 * Date: 16/8/31
 * Tel:  18500234565
 * Des:  换一批题
 */
public class ChangeSetEvent {
    int homeworkDifficulty;

    public ChangeSetEvent(int homeworkDifficulty) {
        this.homeworkDifficulty = homeworkDifficulty;
    }

    public int getHomeworkDifficulty() {
        return homeworkDifficulty;
    }

    public void setHomeworkDifficulty(int homeworkDifficulty) {
        this.homeworkDifficulty = homeworkDifficulty;
    }
}
