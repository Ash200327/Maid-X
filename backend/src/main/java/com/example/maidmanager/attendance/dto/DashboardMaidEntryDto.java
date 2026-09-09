package com.example.maidmanager.attendance.dto;

import java.time.Instant;
import java.util.UUID;

public class DashboardMaidEntryDto {

    private UUID maidId;
    private String maidName;
    private String state;
    private UUID activeSessionId;
    private Instant entryAt;
    private Instant exitAt;
    private int completedSessionsCount;
    private long totalWorkedMinutes;

    public DashboardMaidEntryDto() {
    }

    public DashboardMaidEntryDto(UUID maidId, String maidName, String state, UUID activeSessionId,
                                 Instant entryAt, Instant exitAt, int completedSessionsCount, long totalWorkedMinutes) {
        this.maidId = maidId;
        this.maidName = maidName;
        this.state = state;
        this.activeSessionId = activeSessionId;
        this.entryAt = entryAt;
        this.exitAt = exitAt;
        this.completedSessionsCount = completedSessionsCount;
        this.totalWorkedMinutes = totalWorkedMinutes;
    }

    public UUID getMaidId() {
        return maidId;
    }

    public void setMaidId(UUID maidId) {
        this.maidId = maidId;
    }

    public String getMaidName() {
        return maidName;
    }

    public void setMaidName(String maidName) {
        this.maidName = maidName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UUID getActiveSessionId() {
        return activeSessionId;
    }

    public void setActiveSessionId(UUID activeSessionId) {
        this.activeSessionId = activeSessionId;
    }

    public Instant getEntryAt() {
        return entryAt;
    }

    public void setEntryAt(Instant entryAt) {
        this.entryAt = entryAt;
    }

    public Instant getExitAt() {
        return exitAt;
    }

    public void setExitAt(Instant exitAt) {
        this.exitAt = exitAt;
    }

    public int getCompletedSessionsCount() {
        return completedSessionsCount;
    }

    public void setCompletedSessionsCount(int completedSessionsCount) {
        this.completedSessionsCount = completedSessionsCount;
    }

    public long getTotalWorkedMinutes() {
        return totalWorkedMinutes;
    }

    public void setTotalWorkedMinutes(long totalWorkedMinutes) {
        this.totalWorkedMinutes = totalWorkedMinutes;
    }
}
