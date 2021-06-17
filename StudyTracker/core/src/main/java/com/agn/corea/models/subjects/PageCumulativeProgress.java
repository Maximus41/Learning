package com.agn.corea.models.subjects;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class PageCumulativeProgress {
    @Id
    public long obId;
    public String pageId;
    @Unique
    private String progressId = UUID.randomUUID().toString();
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
    public float totalStoryPointsCovered;

    public String getProgressId() {
        return progressId;
    }
}
