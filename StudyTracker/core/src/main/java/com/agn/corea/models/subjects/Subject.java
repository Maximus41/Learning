package com.agn.corea.models.subjects;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "subjects",
        indices = {@Index(value = "subjectId", unique = true)})
public class Subject {
    @PrimaryKey(autoGenerate = true)
    public long obId;
    public String subjectId = UUID.randomUUID().toString();
    public String subjectTitle;
    public long createdOn;
    public int nosOfSections;

    public String getSubjectId() {
        return subjectId;
    }
}
