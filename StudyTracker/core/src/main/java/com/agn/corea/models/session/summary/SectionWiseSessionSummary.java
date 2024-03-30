package com.agn.corea.models.session.summary;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class SectionWiseSessionSummary {
    @Id
    public long obId;
    public String summaryId;

    @Unique
    public String sectionSummaryId;
    public String sectionId;
    public float sectionStoryPoints;
    public float sectionStoryPointsCovered;
    public long createdOn;
}
