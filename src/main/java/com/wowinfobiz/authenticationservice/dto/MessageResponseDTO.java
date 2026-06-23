package com.wowinfobiz.authenticationservice.dto;

public class MessageResponseDTO {

    private String message;
    private String status;
    private Object body;

    public MessageResponseDTO() {
    }

    public MessageResponseDTO(String message, String status, Object body) {
        this.message = message;
        this.status = status;
        this.body = body;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
