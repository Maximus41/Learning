
package com.poc.corea.models.subjects;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class Page {

    @Id
    public long obId;

    @Unique
    private String pageId;
    private String sectionId;
    private String pageTitle;
    private Integer pageStoryPoints;

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public Integer getPageStoryPoints() {
        return pageStoryPoints;
    }

    public void setPageStoryPoints(Integer pageStoryPoints) {
        this.pageStoryPoints = pageStoryPoints;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }
}
