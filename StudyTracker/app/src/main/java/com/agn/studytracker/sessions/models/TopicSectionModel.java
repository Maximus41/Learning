package com.agn.studytracker.sessions.models;

import com.agn.studytracker.common.adapter.BaseExpandableListAdapter;

public class TopicSectionModel implements BaseExpandableListAdapter.ExpandableListItem {

    private String sectionTitle;
    private String sectionId;
    private long topicObId;
    private String topicId;

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

    public long getTopicObId() {
        return topicObId;
    }

    public void setTopicObId(long topicObId) {
        this.topicObId = topicObId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }
}
