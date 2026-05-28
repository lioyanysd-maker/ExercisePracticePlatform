package com.lio.exercisepracticesystem.dto;

import java.util.List;

public class BindDocImagesRequest {
    private Long userId;
    private Long subjectId;
    private List<DocumentImageItem> images;

    public BindDocImagesRequest() {
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

    public List<DocumentImageItem> getImages() {
        return images;
    }

    public void setImages(List<DocumentImageItem> images) {
        this.images = images;
    }
}

