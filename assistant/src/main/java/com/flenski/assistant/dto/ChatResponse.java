package com.flenski.assistant.dto;

import java.util.List;

public class ChatResponse {
    private String answer;
    private List<SourceInfo> sources;

    public ChatResponse() {}

    public ChatResponse(String answer, List<SourceInfo> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<SourceInfo> getSources() {
        return sources;
    }

    public void setSources(List<SourceInfo> sources) {
        this.sources = sources;
    }
}
