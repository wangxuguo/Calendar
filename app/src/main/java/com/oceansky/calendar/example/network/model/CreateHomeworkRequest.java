package com.oceansky.example.network.model;
/**
 * User: dengfa
 * Date: 16/8/24
 * Tel:  18500234565
 */
public class CreateHomeworkRequest {
    private String title;
    private int grade_id;
    private int lesson_id;

    public CreateHomeworkRequest(String title, int grade_id, int lesson_id) {
        this.title = title;
        this.grade_id = grade_id;
        this.lesson_id = lesson_id;
    }
}
