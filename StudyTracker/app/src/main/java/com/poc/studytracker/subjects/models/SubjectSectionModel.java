package com.poc.studytracker.subjects.models;

import com.poc.corea.models.subjects.SubjectSection;
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter;

public class SubjectSectionModel implements BaseExpandableListAdapter.ExpandableListItem {

    private SubjectSection subjectSection;

    public SubjectSectionModel(SubjectSection subjectSection) {
        this.subjectSection = subjectSection;
    }

    @Override
    public int getType() {
        return BaseExpandableListAdapter.PARENT_TYPE;
    }

    @Override
    public String getObjectId() {
        return subjectSection.getSectionId();
    }

    @Override
    public String getParentObjectId() {
        return null;
    }

    public String getSectionTitle() {
        return subjectSection.getSectionName();
    }
}
