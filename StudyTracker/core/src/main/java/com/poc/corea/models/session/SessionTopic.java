package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class SessionTopic {
    @Id
    public long obId;
    public String sessionId;
    @Unique
    public String topicId;
    public String sectionId;
    public String firstPageId;
    public String secondPageId;
    public float firstPageStoryPoints;
    public float secondPageStoryPoints;
    public float topicStoryPoints;
}
