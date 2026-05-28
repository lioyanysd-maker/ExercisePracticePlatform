package com.lio.exercisepracticesystem.dto;

public class DocumentImageItem {
    private Integer index;
    private String fileName;
    private String contentType;
    private String base64;
    private Integer guessedQuestionNo;
    private Integer mappedQuestionIndex;

    public DocumentImageItem() {
    }

    public DocumentImageItem(Integer index, String fileName, String contentType, String base64, Integer guessedQuestionNo) {
        this.index = index;
        this.fileName = fileName;
        this.contentType = contentType;
        this.base64 = base64;
        this.guessedQuestionNo = guessedQuestionNo;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public Integer getGuessedQuestionNo() {
        return guessedQuestionNo;
    }

    public void setGuessedQuestionNo(Integer guessedQuestionNo) {
        this.guessedQuestionNo = guessedQuestionNo;
    }

    public Integer getMappedQuestionIndex() {
        return mappedQuestionIndex;
    }

    public void setMappedQuestionIndex(Integer mappedQuestionIndex) {
        this.mappedQuestionIndex = mappedQuestionIndex;
    }
}

