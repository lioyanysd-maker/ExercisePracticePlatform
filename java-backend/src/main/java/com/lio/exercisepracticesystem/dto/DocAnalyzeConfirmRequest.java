package com.lio.exercisepracticesystem.dto;

import java.util.List;
import java.util.Map;

public class DocAnalyzeConfirmRequest {
    private Long userId;
    private Long subjectId;
    private List<Map<String, Object>> questions;
    private List<DocumentImageItem> images;

    public DocAnalyzeConfirmRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public List<Map<String, Object>> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Map<String, Object>> questions) {
        this.questions = questions;
    }

    public List<DocumentImageItem> getImages() {
        return images;
    }

    public void setImages(List<DocumentImageItem> images) {
        this.images = images;
    }
}

