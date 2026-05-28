package com.lio.exercisepracticesystem.dto;

import java.util.List;

public class DocumentPreviewResponse {
    private boolean success;
    private String message;
    private String fileType;
    private Integer totalImages;
    private List<Integer> questionNumbers;
    private List<DocumentImageItem> images;

    public DocumentPreviewResponse() {
    }

    public DocumentPreviewResponse(boolean success, String message, String fileType, Integer totalImages,
                                   List<Integer> questionNumbers, List<DocumentImageItem> images) {
        this.success = success;
        this.message = message;
        this.fileType = fileType;
        this.totalImages = totalImages;
        this.questionNumbers = questionNumbers;
        this.images = images;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Integer getTotalImages() {
        return totalImages;
    }

    public void setTotalImages(Integer totalImages) {
        this.totalImages = totalImages;
    }

    public List<Integer> getQuestionNumbers() {
        return questionNumbers;
    }

    public void setQuestionNumbers(List<Integer> questionNumbers) {
        this.questionNumbers = questionNumbers;
    }

    public List<DocumentImageItem> getImages() {
        return images;
    }

    public void setImages(List<DocumentImageItem> images) {
        this.images = images;
    }
}

