package com.poc.studytracker.subjects.models;

import com.poc.studytracker.common.adapter.BaseExpandableListAdapter;

public class SubjectSectionModel implements BaseExpandableListAdapter.ExpandableListItem {

    private String sectionTitle;
    private String sectionId;
    private float sectionProgressPercent;

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

    public float getSectionProgressPercent() {
        return sectionProgressPercent;
    }

    public void setSectionProgressPercent(float sectionProgressPercent) {
        this.sectionProgressPercent = sectionProgressPercent;
    }
}
