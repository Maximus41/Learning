package com.poc.studytracker.subjects.models;

import com.poc.corea.models.subjects.Subject;

public class SubjectUiModel {
    private long noOfSessions;
    private boolean isLastSessionActive;
    private Subject subject;

    public long getNoOfSessions() {
        return noOfSessions;
    }

    public void setNoOfSessions(long noOfSessions) {
        this.noOfSessions = noOfSessions;
    }

    public boolean isLastSessionActive() {
        return isLastSessionActive;
    }

    public void setLastSessionActive(boolean lastSessionActive) {
        isLastSessionActive = lastSessionActive;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
