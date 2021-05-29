
package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class Assessment {

    @Id
    public long obId;

    @Unique
    private String assessmentId;
    private String sessionSummary;
    private String nextSessionPlanningSummary;

    public String getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(String sessionSummary) {
        this.sessionSummary = sessionSummary;
    }

    public String getNextSessionPlanningSummary() {
        return nextSessionPlanningSummary;
    }

    public void setNextSessionPlanningSummary(String nextSessionPlanningSummary) {
        this.nextSessionPlanningSummary = nextSessionPlanningSummary;
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }
}
