package com.oceansky.calendar.example.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * User: dengfa
 * Date: 16/8/30
 * Tel:  18500234565
 */
public class HomeworkEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("question_id")
    private String question_id;

    @SerializedName("question_html")
    private String question_html;

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getQuestion_html() {
        return question_html;
    }

    public void setQuestion_html(String question_html) {
        this.question_html = question_html;
    }

    @Override
    public String toString() {
        return "HomeworkEntity{" +
                "question_id='" + question_id + '\'' +
                ", question_html='" + question_html + '\'' +
                '}';
    }
}
