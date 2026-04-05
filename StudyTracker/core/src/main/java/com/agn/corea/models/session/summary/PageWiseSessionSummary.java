package com.agn.corea.models.session.summary;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "page_wise_session_summaries",
        indices = {
                @Index(value = "pageSummaryId", unique = true),
                @Index(value = "summaryId"),
                @Index(value = "pageId")
        })
public class PageWiseSessionSummary {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String summaryId;
    public String pageId;
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
