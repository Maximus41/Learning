package com.agn.corea.models.session.summary;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "section_wise_session_summaries",
        indices = {
                @Index(value = "sectionSummaryId", unique = true),
                @Index(value = "summaryId")
        })
public class SectionWiseSessionSummary {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String summaryId;
    public String sectionSummaryId;
    public String sectionId;
    public float sectionStoryPoints;
    public float sectionStoryPointsCovered;
    public long createdOn;
}
