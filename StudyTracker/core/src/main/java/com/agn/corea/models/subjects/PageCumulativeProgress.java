package com.agn.corea.models.subjects;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "page_cumulative_progress",
        indices = {
                @Index(value = "progressId", unique = true),
                @Index(value = "pageId")
        })
public class PageCumulativeProgress {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String pageId;
    public String progressId = UUID.randomUUID().toString();
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
