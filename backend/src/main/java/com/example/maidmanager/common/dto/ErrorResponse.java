package com.example.maidmanager.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code;
    private String message;
    private Map<String, String> fieldErrors;
    private String requestId;

    public ErrorResponse() {
        String mdcId = org.slf4j.MDC.get("requestId");
        this.requestId = (mdcId != null && !mdcId.isBlank()) ? mdcId : UUID.randomUUID().toString();
    }

    public ErrorResponse(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String code, String message, Map<String, String> fieldErrors) {
        this(code, message);
        this.fieldErrors = fieldErrors;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
