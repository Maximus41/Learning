package com.agn.corea.models.session;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "sessions",
        indices = {
                @Index(value = "sessionId", unique = true),
                @Index(value = "subjectId")
        })
public class Session {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String subjectId;
    public String sessionId = UUID.randomUUID().toString();
    public String sessionTitle;
    public int sessionSerialNo;
    public long createdOn;
    public long startedOn;
    public long expiresOn;
    public long endedOn;
    public boolean hasSessionExpired;
    public boolean hasSessionEnded;
    public boolean isSessionActive;
    public boolean isSessionAssessed;
    public float sessionStoryPoints;

    public String getSessionId() {
        return sessionId;
    }
}
