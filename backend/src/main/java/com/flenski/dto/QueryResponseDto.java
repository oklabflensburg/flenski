package com.flenski.dto;

import java.util.List;

public class QueryResponseDto {
    private String answer;
    private List<DocumentDto> documents;

    public QueryResponseDto(String answer, List<DocumentDto> documents) {
        this.answer = answer;
        this.documents = documents;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<DocumentDto> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDto> documents) {
        this.documents = documents;
    }
}
