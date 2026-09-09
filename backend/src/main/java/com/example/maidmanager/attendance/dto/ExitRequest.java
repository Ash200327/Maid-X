package com.example.maidmanager.attendance.dto;

public class ExitRequest {

    private String clientRequestId;
    private String note;

    public ExitRequest() {
    }

    public ExitRequest(String clientRequestId, String note) {
        this.clientRequestId = clientRequestId;
        this.note = note;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
