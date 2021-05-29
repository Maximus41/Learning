package com.poc.studytracker.subjects.models;


import com.poc.studytracker.common.adapter.BaseExpandableListAdapter;

public class SubjectPageModel implements BaseExpandableListAdapter.ExpandableListItem {

//    private Page page;

    @Override
    public int getType() {
        return BaseExpandableListAdapter.CHILD_TYPE;
    }

    @Override
    public String getObjectId() {
        return "page.getPageId()";
    }

    @Override
    public String getParentObjectId() {
        return "page.getSectionId()";
    }

//    public Page getPage() {
//        return page;
//    }
}
