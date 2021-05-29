
package com.poc.corea.models.summary;

import com.poc.corea.models.obconverters.CumulativeStatusConverter;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class SummaryPage {

    @Id
    public long obId;
    private String pageName;

    @Unique
    private String pageId;
    private Integer pageStoryPoints;

    @Convert(converter = CumulativeStatusConverter.class, dbType = String.class)
    private CumulativeStatus status;

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

    public CumulativeStatus getStatus() {
        return status;
    }

    public void setStatus(CumulativeStatus status) {
        this.status = status;
    }

}
