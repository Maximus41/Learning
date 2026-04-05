package com.agn.corea.models.subjects;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.agn.corea.constants.GlobalConstants;

import java.util.UUID;

@Entity(tableName = "pages",
        indices = {
                @Index(value = "pageId", unique = true),
                @Index(value = "sectionId")
        })
public class Page {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String sectionId;
    public String pageId = UUID.randomUUID().toString();
    public String pageTitle;
    public long createdOn;
    public float pageStoryPoints = GlobalConstants.DEFAULT_PAGE_STORY_POINTS;

    public String getPageId() {
        return pageId;
    }
}
