package com.example.maidmanager.attendance.dto;

import java.time.LocalDate;

public class EntryRequest {

    private LocalDate businessDate;
    private String clientRequestId;
    private String note;

    public EntryRequest() {
    }

    public EntryRequest(LocalDate businessDate, String clientRequestId, String note) {
        this.businessDate = businessDate;
        this.clientRequestId = clientRequestId;
        this.note = note;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
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
