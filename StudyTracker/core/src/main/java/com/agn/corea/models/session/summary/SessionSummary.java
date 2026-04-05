package com.agn.corea.models.session.summary;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "session_summaries",
        indices = {
                @Index(value = "summaryId", unique = true),
                @Index(value = "sessionId")
        })
public class SessionSummary {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String sessionId;
    public String summaryId;
    public float totalStoryPoints;
    public float totalStoryPointsCovered;
    public long createdOn;
}
