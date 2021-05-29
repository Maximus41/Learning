
package com.poc.corea.models.subjects;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class SubjectSection {
    @Id
    public long obId;

    @Unique
    private String sectionId;
    private String sectionName;
    private Integer sectionTotalStoryPoints;
    public ToMany<Page> pages;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Integer getSectionTotalStoryPoints() {
        return sectionTotalStoryPoints;
    }

    public void setSectionTotalStoryPoints(Integer sectionTotalStoryPoints) {
        this.sectionTotalStoryPoints = sectionTotalStoryPoints;
    }
}
