package com.agn.corea.models.subjects;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "sections",
        indices = {
                @Index(value = "sectionId", unique = true),
                @Index(value = "subjectId")
        })
public class Section {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String subjectId;
    public String sectionId = UUID.randomUUID().toString();
    public String sectionTitle;
    public long createdOn;
    public int noOfPages;
    public float totalStoryPoints;

    public String getSectionId() {
        return sectionId;
    }
}
