package com.agn.studytracker.sessions.models;

import com.agn.studytracker.common.adapter.BaseExpandableListAdapter;

public class UpdateTopicSectionModel implements BaseExpandableListAdapter.ExpandableListItem {
    private String sectionTitle;
    private String sectionId;
    private int sessionTopicProgressPercent;

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

    public int getSessionTopicProgressPercent() {
        return sessionTopicProgressPercent;
    }

    public void setSessionTopicProgressPercent(int sessionTopicProgressPercent) {
        this.sessionTopicProgressPercent = sessionTopicProgressPercent;
    }
}
