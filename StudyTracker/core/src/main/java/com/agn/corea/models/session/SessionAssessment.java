package com.agn.corea.models.session;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "session_assessments",
        indices = {
                @Index(value = "assessmentId", unique = true),
                @Index(value = "sessionId")
        })
public class SessionAssessment {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String sessionId;
    public String assessmentId = UUID.randomUUID().toString();
    public String sessionSummary;
    public String nextSessionPlan;
    public String questions;
    public String todos;

    public String getAssessmentId() {
        return assessmentId;
    }
}
