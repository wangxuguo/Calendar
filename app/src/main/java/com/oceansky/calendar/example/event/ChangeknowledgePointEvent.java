package com.oceansky.example.event;

/**
 * User: dengfa
 * Date: 16/9/1
 * Tel:  18500234565
 */
public class ChangeknowledgePointEvent {

    private int textbook_ids;
    private int knowledge_chapter_ids;
    private int knowledge_section_ids;
    private int knowledge_detail_ids;
    private String knowledge_detail;

    public ChangeknowledgePointEvent(int textbook_ids, int knowledge_chapter_ids, int knowledge_section_ids,
                                     int knowledge_detail_ids, String knowledge_detail) {
        this.textbook_ids = textbook_ids;
        this.knowledge_chapter_ids = knowledge_chapter_ids;
        this.knowledge_section_ids = knowledge_section_ids;
        this.knowledge_detail_ids = knowledge_detail_ids;
        this.knowledge_detail = knowledge_detail;
    }

    public int getTextbook_ids() {
        return textbook_ids;
    }

    public void setTextbook_ids(int textbook_ids) {
        this.textbook_ids = textbook_ids;
    }

    public int getKnowledge_chapter_ids() {
        return knowledge_chapter_ids;
    }

    public void setKnowledge_chapter_ids(int knowledge_chapter_ids) {
        this.knowledge_chapter_ids = knowledge_chapter_ids;
    }

    public int getKnowledge_section_ids() {
        return knowledge_section_ids;
    }

    public void setKnowledge_section_ids(int knowledge_section_ids) {
        this.knowledge_section_ids = knowledge_section_ids;
    }

    public int getKnowledge_detail_ids() {
        return knowledge_detail_ids;
    }

    public void setKnowledge_detail_ids(int knowledge_detail_ids) {
        this.knowledge_detail_ids = knowledge_detail_ids;
    }

    public String getKnowledge_detail() {
        return knowledge_detail;
    }

    public void setKnowledge_detail(String knowledge_detail) {
        this.knowledge_detail = knowledge_detail;
    }

    @Override
    public String toString() {
        return "ChangeknowledgePointEvent{" +
                "textbook_ids=" + textbook_ids +
                ", knowledge_chapter_ids=" + knowledge_chapter_ids +
                ", knowledge_section_ids=" + knowledge_section_ids +
                ", knowledge_detail_ids=" + knowledge_detail_ids +
                ", knowledge_detail='" + knowledge_detail + '\'' +
                '}';
    }
}
