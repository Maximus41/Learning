package in.agn.studytracker.sessions.models;

import in.agn.studytracker.common.adapter.BaseExpandableListAdapter;

public class TopicPageModel implements BaseExpandableListAdapter.ExpandableListItem {

    private String sectionId;
    private String pageTitle;
    private String pageId;
    private String paraformattedContent;
    private String sessionTopicId;

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

    public String getSessionTopicId() {
        return sessionTopicId;
    }

    public void setSessionTopicId(String sessionTopicId) {
        this.sessionTopicId = sessionTopicId;
    }
}
