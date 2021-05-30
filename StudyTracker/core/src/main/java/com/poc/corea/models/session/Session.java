package com.poc.corea.models.session;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class Session {
    @Id
    public long obId;
    public String subjectId;
    @Unique
    private String sessionId = UUID.randomUUID().toString();
    public String sessionTitle;
    public int sessionSerialNo;
    public long createdOn;
    public long startedOn;
    public long expiresOn;
    public long endedOn;
    public boolean hasSessionExpired;
    public boolean hasSessionEnded;
    public boolean isSessionActive;
    public ToMany<SessionTopic> topics;

    public String getSessionId() {
        return sessionId;
    }
}
