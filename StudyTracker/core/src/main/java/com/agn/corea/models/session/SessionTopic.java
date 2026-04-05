package com.agn.corea.models.session;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "session_topics",
        indices = {
                @Index(value = "topicId", unique = true),
                @Index(value = "sessionId")
        })
public class SessionTopic {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String sessionId;
    public String topicId = UUID.randomUUID().toString();
    public String sectionId;
    public String sectionTitle;
    public String firstPageId;
    public String firstPageTitle;
    public String secondPageTitle;
    public String secondPageId;
    public float firstPageStoryPoints;
    public float secondPageStoryPoints;
    public float topicStoryPoints;
    public long createdOn;

    public String getTopicId() {
        return topicId;
    }
}
