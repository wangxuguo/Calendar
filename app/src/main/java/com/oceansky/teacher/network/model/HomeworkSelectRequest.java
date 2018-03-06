package com.oceansky.teacher.network.model;

/**
 * User: dengfa
 * Date: 16/8/30
 * Tel:  18500234565
 */
public class HomeworkSelectRequest {
    private int    lesson;
    private String knowledge_points;
    private String knowledge_detail_ids;
    private int    question_type;
    private int    difficulty;
    private String offset;
    private int    size;

    public HomeworkSelectRequest(int lesson, String knowledge_detail_ids, String knowledge_points,
                                 int question_type, int difficulty, String offset, int size) {
        this.lesson = lesson;
        this.knowledge_points = knowledge_points;
        this.knowledge_detail_ids = knowledge_detail_ids;
        this.question_type = question_type;
        this.difficulty = difficulty;
        this.offset = offset;
        this.size = size;
    }
}
