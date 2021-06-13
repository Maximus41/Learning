package com.poc.corea.models.session;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class SessionAssessment {

    @Id
    public long obId;
    public String sessionId;
    @Unique
    private String assessmentId = UUID.randomUUID().toString();
    public String sessionSummary;
    public String nextSessionPlan;
    public String questions;
    public String todos;

    public String getAssessmentId() {
        return assessmentId;
    }
}
