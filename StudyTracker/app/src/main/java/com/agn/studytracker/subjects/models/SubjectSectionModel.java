package com.agn.studytracker.subjects.models;

import com.agn.studytracker.common.adapter.BaseExpandableListAdapter;

public class SubjectSectionModel implements BaseExpandableListAdapter.ExpandableListItem {

    private String sectionTitle;
    private String sectionId;
    private int sectionProgressPercent;

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

    public int getSectionProgressPercent() {
        return sectionProgressPercent;
    }

    public void setSectionProgressPercent(int sectionProgressPercent) {
        this.sectionProgressPercent = sectionProgressPercent;
    }
}
