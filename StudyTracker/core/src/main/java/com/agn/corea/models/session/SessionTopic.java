package com.agn.corea.models.session;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class SessionTopic {
    @Id
    public long obId;
    public String sessionId;
    @Unique
    private String topicId = UUID.randomUUID().toString();
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
