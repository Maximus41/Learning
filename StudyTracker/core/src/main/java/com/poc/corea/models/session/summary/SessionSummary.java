package com.poc.corea.models.session.summary;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class SessionSummary {
    @Id
    public long obId;
    public String sessionId;
    @Unique
    public String summaryId;
    public float totalStoryPoints;
    public float totalStoryPointsCovered;
    public long createdOn;
}
