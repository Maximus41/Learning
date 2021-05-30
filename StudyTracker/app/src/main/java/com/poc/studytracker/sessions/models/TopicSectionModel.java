package com.poc.studytracker.sessions.models;

import com.poc.studytracker.common.adapter.BaseExpandableListAdapter;

public class TopicSectionModel implements BaseExpandableListAdapter.ExpandableListItem {

    private String sectionTitle;
    private String sectionId;

    @Override
    public int getType() {
        return BaseExpandableListAdapter.PARENT_TYPE;
    }

    @Override
    public String getObjectId() {
        return sectionId;
    }

    @Override
    public String getParentObjectId() {
        return null;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }
}
