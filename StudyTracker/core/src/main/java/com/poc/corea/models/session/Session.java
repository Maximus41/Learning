package com.poc.corea.models.session;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

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

    public String getSessionId() {
        return sessionId;
    }
}
