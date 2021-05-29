
package com.poc.corea.models.session;

import com.poc.corea.models.obconverters.StatusConverter;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class SessionPage {
    @Id
    public long obId;
    private String pageName;
    @Unique
    private String pageId;
    private Integer pageStoryPoints;

    @Convert(converter = StatusConverter.class, dbType = String.class)
    private Status status;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
