package com.poc.corea.models.subjects;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class Page {
    @Id
    public long obId;
    public String sectionId;
    @Unique
    private String pageId = UUID.randomUUID().toString();
    public String pageTitle;
    public long createdOn;
    public float pageStoryPoints;
    public ToMany<Para> paras;

    public String getPageId() {
        return pageId;
    }
}
