package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wowinfobiz.configurationservice.service.JsonMapperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final JsonMapperService jsonMapperService;

    public ApiExceptionHandler(JsonMapperService jsonMapperService) {
        this.jsonMapperService = jsonMapperService;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ObjectNode> handleIllegalArgument(IllegalArgumentException ex) {
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
