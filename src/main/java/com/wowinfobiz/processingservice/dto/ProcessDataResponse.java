package com.wowinfobiz.processingservice.dto;

import java.util.Map;

public class ProcessDataResponse<T> {
    private  String message;
    private boolean status;
    private Map<String, Object> body;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    public ProcessDataResponse(String message, boolean status, Map<String, Object> body) {
        this.message = message;
        this.status = status;
        this.body = body;
    }
}
