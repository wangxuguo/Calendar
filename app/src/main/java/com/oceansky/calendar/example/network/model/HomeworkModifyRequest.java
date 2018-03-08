package com.oceansky.example.network.model;

/**
 * User: dengfa
 * Date: 16/8/30
 * Tel:  18500234565
 */
public class HomeworkModifyRequest {
    private String title;
    private String textbook_ids;
    private String knowledge_chapter_ids;
    private String knowledge_section_ids;
    private String knowledge_detail_ids;
    private String question_ids;

    public HomeworkModifyRequest(String title) {
        this.title = title;
    }

    public HomeworkModifyRequest(String textbook_ids, String knowledge_chapter_ids, String knowledge_section_ids,
                                 String knowledge_detail_ids, String question_ids) {
        this.textbook_ids = textbook_ids;
        this.knowledge_chapter_ids = knowledge_chapter_ids;
        this.knowledge_section_ids = knowledge_section_ids;
        this.knowledge_detail_ids = knowledge_detail_ids;
        this.question_ids = question_ids;
    }
}
