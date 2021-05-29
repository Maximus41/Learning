package com.poc.corea.models.session.summary;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class PageWiseSessionSummary {
    @Id
    public long obId;
    public String summaryId;
    public String pageId;
    @Unique
    public String pageSummaryId;
    public int readStatus;
    public long readStatusRecordedOn;
    public int notesTakenStatus;
    public long notesStatusRecordedOn;
    public int memorizedStatus;
    public long memorizedStatusRecordedOn;
    public int reviewCount;
    public long lastReviewedOn;
    public int practiceCount;
    public long lastPracticedOn;
    public float totalStoryPoints;
    public float totalStoryPointsCovered;
    public String readSessionNo;
    public String notesTakenSessionNo;
    public String memorizedSessionNo;
}
