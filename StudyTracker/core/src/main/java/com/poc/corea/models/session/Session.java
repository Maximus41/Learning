
package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class Session {
    @Id
    public long obId;
    @Unique
    private String sessionId;
    private Integer sessionOrder;
    private Integer sessionStoryPoints;
    private String subjectId;
    private String sessionTitle;
    public ToOne<SessionStudyTopics> sessionStudyTopics;
    public ToOne<Assessment> assessment;
    public ToOne<Next> next;
    private long sessionCreatedOn;
    private long sessionStartedOn;
    private long sessionExpiresOn;
    private long sessionEndedOn;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getSessionOrder() {
        return sessionOrder;
    }

    public void setSessionOrder(Integer sessionOrder) {
        this.sessionOrder = sessionOrder;
    }

    public Integer getSessionStoryPoints() {
        return sessionStoryPoints;
    }

    public void setSessionStoryPoints(Integer sessionStoryPoints) {
        this.sessionStoryPoints = sessionStoryPoints;
    }

    public long getSessionCreatedOn() {
        return sessionCreatedOn;
    }

    public void setSessionCreatedOn(long sessionCreatedOn) {
        this.sessionCreatedOn = sessionCreatedOn;
    }

    public long getSessionStartedOn() {
        return sessionStartedOn;
    }

    public void setSessionStartedOn(long sessionStartedOn) {
        this.sessionStartedOn = sessionStartedOn;
    }

    public long getSessionExpiresOn() {
        return sessionExpiresOn;
    }

    public void setSessionExpiresOn(long sessionExpiresOn) {
        this.sessionExpiresOn = sessionExpiresOn;
    }

    public long getSessionEndedOn() {
        return sessionEndedOn;
    }

    public void setSessionEndedOn(long sessionEndedOn) {
        this.sessionEndedOn = sessionEndedOn;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
    }
}
