package com.poc.studytracker.sessions.models;

import com.poc.studytracker.common.adapter.BaseExpandableListAdapter;

public class TopicPageModel implements BaseExpandableListAdapter.ExpandableListItem {

    private String sectionId;
    private String pageTitle;
    private String pageId;
    private String paraformattedContent;

    @Override
    public int getType() {
        return BaseExpandableListAdapter.CHILD_TYPE;
    }

    @Override
    public String getObjectId() {
        return pageId;
    }

    @Override
    public String getParentObjectId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getParaformattedContent() {
        return paraformattedContent;
    }

    public void setParaformattedContent(String paraformattedContent) {
        this.paraformattedContent = paraformattedContent;
    }
}