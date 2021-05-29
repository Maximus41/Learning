package com.poc.corea.models.subjects;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class Para {
    @Id
    public long obId;
    public String pageId;
    @Unique
    private String paraId = UUID.randomUUID().toString();
    public String paraTitle;
    public long createdOn;
    public float storyPoint = 0.15f;

    public String getParaId() {
        return paraId;
    }
}
