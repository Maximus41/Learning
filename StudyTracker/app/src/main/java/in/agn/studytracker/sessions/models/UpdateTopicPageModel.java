package in.agn.studytracker.sessions.models;

import in.agn.studytracker.common.adapter.BaseExpandableListAdapter;

public class UpdateTopicPageModel implements BaseExpandableListAdapter.ExpandableListItem {
    private String sectionId;
    private String pageTitle;
    private String pageId;

    private boolean isRead;
    private boolean isNotesTaken;
    private boolean isMemorized;
    private int reviewCount;
    private int practiceCount;
    private boolean actionsFreezed;

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

    public boolean isNotesTaken() {
        return isNotesTaken;
    }

    public void setNotesTaken(boolean notesTaken) {
        isNotesTaken = notesTaken;
    }

    public boolean isMemorized() {
        return isMemorized;
    }

    public void setMemorized(boolean memorized) {
        isMemorized = memorized;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getPracticeCount() {
        return practiceCount;
    }

    public void setPracticeCount(int practiceCount) {
        this.practiceCount = practiceCount;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isActionsFreezed() {
        return actionsFreezed;
    }

    public void setActionsFreezed(boolean actionsFreezed) {
        this.actionsFreezed = actionsFreezed;
    }
}
