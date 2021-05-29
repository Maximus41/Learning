
package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class NextSectionPage {

    @Id
    public long obId;
    private String pageName;
    @Unique
    private String pageId;
    private Integer pageStoryPoints;

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public Integer getPageStoryPoints() {
        return pageStoryPoints;
    }

    public void setPageStoryPoints(Integer pageStoryPoints) {
        this.pageStoryPoints = pageStoryPoints;
    }

}
