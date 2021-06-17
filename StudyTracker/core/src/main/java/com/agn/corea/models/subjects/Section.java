package com.agn.corea.models.subjects;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class Section {
    @Id
    public long obId;
    public String subjectId;
    @Unique
    private String sectionId = UUID.randomUUID().toString();
    public String sectionTitle;
    public long createdOn;
    public int noOfPages;
    public float totalStoryPoints;
    public ToMany<Page> pages;

    public String getSectionId() {
        return sectionId;
    }
}
