package com.poc.corea.models.subjects;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;
import io.objectbox.relation.ToOne;

@Entity
public class Page {
    @Id
    public long obId;
    public String sectionId;
    @Unique
    private String pageId = UUID.randomUUID().toString();
    public String pageTitle;
    public long createdOn;
    public float pageStoryPoints = 0.5f;
    public ToMany<Para> paras;
    public ToOne<PageCumulativeProgress> progress;

    public String getPageId() {
        return pageId;
    }
}
