package com.oceansky.teacher.event;

/**
 * User: dengfa
 * Date: 16/9/28
 * Tel:  18500234565
 */
public class LoginSuccessEvent {
    private String className;

    public LoginSuccessEvent(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
