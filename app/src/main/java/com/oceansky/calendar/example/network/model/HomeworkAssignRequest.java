package com.oceansky.example.network.model;

/**
 * User: dengfa
 * Date: 16/8/30
 * Tel:  18500234565
 */
public class HomeworkAssignRequest {
    private int course_id;
    private String publish_time;
    private String end_time;
    private int answer_publish_type;

    public HomeworkAssignRequest(int course_id, String publish_time, String end_time, int answer_publish_type) {
        this.course_id = course_id;
        this.publish_time = publish_time;
        this.end_time = end_time;
        this.answer_publish_type = answer_publish_type;
    }
}
