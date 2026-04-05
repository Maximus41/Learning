package com.agn.corea.models.subjects;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.agn.corea.constants.GlobalConstants;

import java.util.UUID;

@Entity(tableName = "paras",
        indices = {
                @Index(value = "paraId", unique = true),
                @Index(value = "pageId")
        })
public class Para {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String pageId;
    public String paraId = UUID.randomUUID().toString();
    public String paraTitle;
    public long createdOn;
    public float storyPoint = GlobalConstants.DEFAULT_PARA_STORY_POINTS;

    public String getParaId() {
        return paraId;
    }
}
